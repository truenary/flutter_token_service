import Flutter
import UIKit
import Foundation

@objc public class SwiftTokenServicePlugin: NSObject, FlutterPlugin {

  static let serverName: String = "tokenservice.truenary.com"

  static let accessTokenLabel: String = "ACCESS_TOKEN"
  static let refreshTokenLabel: String = "REFRESH_TOKEN"

  let tokenService: TokenService = TokenService.init(serverName: SwiftTokenServicePlugin.serverName, accessTokenLabel: SwiftTokenServicePlugin.accessTokenLabel, refreshTokenLabel: SwiftTokenServicePlugin.refreshTokenLabel)

  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "\(SwiftTokenServicePlugin.serverName)/tokens", binaryMessenger: registrar.messenger())
    let instance = SwiftTokenServicePlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {

    do {

      switch (call.method) {
      case "getAccessToken": result(try self.tokenService.getAccessToken())
      case "getRefreshToken": result(try self.tokenService.getRefreshToken())
      case "addOrUpdateAccessToken": result(try self.tokenService.addOrUpdateAccessToken(token: call.arguments as! String))
      case "deleteAccessToken": result(try self.tokenService.deleteAccessToken())
      default: result(FlutterMethodNotImplemented)
      }
    } catch TokenKeychainError.corruptData {
      // log error
      result(FlutterError(code: "502", message: "Inconsistent Token", details: nil))
    } catch TokenKeychainError.unhandledError(let osStatus) {
      // log error
      result(FlutterError(code: "502", message: "Token Unhandled Error: \(osStatus)", details: nil))
    } catch {
      // log error
      result(FlutterError(code: "502", message: "Token Error: \(error)", details: nil))
    }
  }
}

enum TokenKeychainError: Error {
  case noToken
  case corruptData
  case unhandledError(error: OSStatus)
}

class TokenService {
  let serverName: String
  let accessTokenLabel: String
  let refreshTokenLabel: String

  let baseQuery: [String: Any]

  init(serverName: String, accessTokenLabel: String, refreshTokenLabel: String) {
    self.serverName = serverName
    self.accessTokenLabel = accessTokenLabel
    self.refreshTokenLabel = refreshTokenLabel

    self.baseQuery = [
      kSecClass as String: kSecClassInternetPassword,
      kSecAttrServer as String: self.serverName,
      kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlocked
    ]
  }

  private func buildAccessTokenQuery() -> [String: Any] {
    return self.baseQuery.merging([
      kSecMatchLimit as String: kSecMatchLimitOne,
      kSecAttrLabel as String: self.accessTokenLabel
    ]) {(_, new) in new}
  }

  private func buildRefreshTokenQuery() -> [String: Any] {
    return self.baseQuery.merging([
      kSecMatchLimitOne as String: kSecMatchLimitOne,
      kSecAttrLabel as String: self.refreshTokenLabel
    ]) {(_, new) in new}
  }

  private func getToken(query: [String: Any]) throws -> String? {
    var item: CFTypeRef?
    let matchQuery = query.merging([
      kSecReturnAttributes as String: true,
      kSecReturnData as String: true
    ]){(_, new) in new}
    let status = SecItemCopyMatching(matchQuery as CFDictionary, &item)
    guard status != errSecItemNotFound else {
      return nil
    }
    guard status == errSecSuccess else {
      throw TokenKeychainError.unhandledError(error: status)
    }

    guard let existingItem = item as? [String : Any],
      let tokenData = existingItem[kSecValueData as String] as? Data,
      let token = String(data: tokenData, encoding: String.Encoding.utf8) else {
        throw TokenKeychainError.corruptData
    }
    return token
  }

  private func addOrUpdateToken(token: String, label: String) throws -> NSNumber {
    let attribute: [String: Any] = [
      kSecAttrAccount as String: "api",
      kSecValueData as String: token.data(using: String.Encoding.utf8)!,
    ]
    let matchQuery: [String: Any] = self.baseQuery.merging([
      kSecAttrLabel as String: label
    ]){(_, new) in new}

    let updateStatus = SecItemUpdate(matchQuery as CFDictionary, attribute as CFDictionary)
    guard updateStatus != errSecSuccess else {
      return 1
    }
    guard updateStatus != errSecItemNotFound else {
      let addStatus = SecItemAdd(
        matchQuery.merging(attribute){(_, new) in new} as CFDictionary,
        nil
      )
      guard addStatus != errSecSuccess else {
        return 1
      }

      throw TokenKeychainError.unhandledError(error: addStatus)
    }

    throw TokenKeychainError.unhandledError(error: updateStatus)
  }

  private func deleteToken(label: String) throws -> NSNumber {
    let matchQuery: [String: Any] = self.baseQuery.merging([
      kSecAttrLabel as String: label
    ]){(_, new) in new}
    let deleteStatus = SecItemDelete(matchQuery as CFDictionary)
    guard deleteStatus == errSecSuccess || deleteStatus == errSecItemNotFound else {
      throw TokenKeychainError.unhandledError(error: deleteStatus)
    }

    return 1
  }

  func getAccessToken() throws -> String? { return try self.getToken(query: self.buildAccessTokenQuery()) }
  func getRefreshToken() throws -> String? { return try self.getToken(query: self.buildRefreshTokenQuery()) }

  func addOrUpdateAccessToken(token: String) throws -> NSNumber { return try self.addOrUpdateToken(token: token, label: self.accessTokenLabel) }
  func addOrUpdateRefreshToken(token: String) throws -> NSNumber { return try self.addOrUpdateToken(token: token, label: self.refreshTokenLabel) }

  func deleteAccessToken() throws -> NSNumber { return try self.deleteToken(label: self.accessTokenLabel) }
  func deleteRefreshToken() throws -> NSNumber { return try self.deleteToken(label: self.refreshTokenLabel) }
}
