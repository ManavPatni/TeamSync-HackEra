import 'package:flutter/material.dart';

class SplashScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Stack(children: [
        Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Image(
                  image: AssetImage('assets/images/ic_logo_transparent.png'),
                  width: 130,
                  height: 150),
              Text(
                "TeamSync",
                style: TextStyle(
                    fontFamily: "Poppins",
                    fontWeight: FontWeight.w600,
                    fontSize: 24),
              )
            ],
          ),
        ),
        Positioned(
            bottom: 20,
            left: 0,
            right: 0,
            child: Text("Syncing Teams,\nStreamlining Success",
                textAlign: TextAlign.center,
                style: TextStyle(
                    fontFamily: "Poppins",
                    fontWeight: FontWeight.w400,
                    fontSize: 15)))
      ]),
    );
  }
}
