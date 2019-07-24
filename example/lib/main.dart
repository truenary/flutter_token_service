import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:token_service/token_service.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String token = 'ejolvnwjaewbeacknhuw4443y92oq';

  String msg = '';

  TokenService tokenservice = TokenService();
  addToken() async {
    tokenservice.addOrUpdateAccessToken(token).then((_) {
      setState(() {
        msg = 'Token Stored';
      });
    });
  }

  deleteToken() async {
    tokenservice.deleteAccessToken().then((_) {
      setState(() {
        msg = 'Token Deleted';
      });
    });
  }

  getToken() async {
    tokenservice.getAccessToken().then((val) {
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
                  RaisedButton(child: Text('Add Token'), onPressed: addToken),
            ),
            Container(
              child:
                  RaisedButton(child: Text('Get Token'), onPressed: getToken),
            ),
            Container(
                child: RaisedButton(
              child: Text('Delete Token'),
              onPressed: deleteToken,
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
