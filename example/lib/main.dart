import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:token_service/token_service.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String accessToken = 'accesswjaewbeacknhuw4443y92oq';
  String refreshToken = 'refreshwjaewbeacknhuw4443y92oq';

  String msg = '';

  TokenService tokenservice = TokenService();
  addAccessToken() async {
    tokenservice.addOrUpdateAccessToken(accessToken).then((_) {
      setState(() {
        msg = 'Token Stored';
      });
    });
  }

  addRefreshToken() async {
    tokenservice.addOrUpdateRefreshToken(refreshToken).then((_) {
      setState(() {
        msg = 'Refresh Token Stored';
      });
    });
  }

  deleteAccessToken() async {
    tokenservice.deleteAccessToken().then((_) {
      setState(() {
        msg = 'Access Token Deleted';
      });
    });
  }

  deleteRefreshToken() async {
    tokenservice.deleteRefreshToken().then((_) {
      setState(() {
        msg = 'Refresh Token Deleted';
      });
    });
  }

  getAccessToken() async {
    tokenservice.getAccessToken().then((val) {
      setState(() {
        msg = val ?? 'Not Token Stored';
      });
    });
  }

  getRefreshToken() async {
    tokenservice.getRefreshToken().then((val) {
      setState(() {
        msg = val ?? 'Not Token Stored';
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Token Service Example app'),
        ),
        body: ListView(
          children: <Widget>[
            Container(
              child:
                  RaisedButton(child: Text('Add Access Token'), onPressed: addAccessToken),
            ),
            Container(
              child:
                  RaisedButton(child: Text('Get Access Token'), onPressed: getAccessToken),
            ),
            Container(
                child: RaisedButton(
              child: Text('Delete Access Token'),
              onPressed: deleteAccessToken,
            )),
            Container(
              child:
                  RaisedButton(child: Text('Add Refresh Token'), onPressed: addRefreshToken),
            ),
            Container(
              child:
                  RaisedButton(child: Text('Get Refresh Token'), onPressed: getRefreshToken),
            ),
            Container(
                child: RaisedButton(
              child: Text('Delete Refresh Token'),
              onPressed: deleteRefreshToken,
            )),
            Container(
              margin: EdgeInsets.only(top: 25),
              alignment: Alignment.center,
              child: Text(msg),
            )
          ],
        ),
      ),
    );
  }
}
