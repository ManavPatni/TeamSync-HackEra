import 'package:flutter/material.dart';
import 'package:google_sign_in/google_sign_in.dart';

class SignIn extends StatelessWidget {

   //GoogleSignIn instance
  final GoogleSignIn _googleSignIn = GoogleSignIn(
    scopes: [
      'email',
    ],
  );

  // Function for Google Sign-In as Admin
  Future<void> _handleAdminSignIn() async {
    try {
      final GoogleSignInAccount? account = await _googleSignIn.signIn();
      if (account != null) {
        // Successfully signed in
        print('Admin signed in: ${account.email}');
        // TODO: Navigate to admin dashboard or other relevant page
      } else {
        // The user canceled the sign-in
        print('Admin sign-in canceled');
      }
    } catch (error) {
      print('Error signing in as admin: $error');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Padding(
        padding:
        const EdgeInsets.symmetric(horizontal: 20, vertical: 50), // Consistent padding
        child: Stack(
          children: [
            // Top-left Logo and Title
            Row(
              children: [
                Image.asset(
                  "assets/images/ic_logo_transparent.png",
                  width: 41,
                  height: 41,
                ),
                const SizedBox(width: 10),
                const Text(
                  "TeamSync",
                  style: TextStyle(
                    fontFamily: "Poppins",
                    fontWeight: FontWeight.w600,
                    fontSize: 20,
                  ),
                ),
              ],
            ),
            // Centered Buttons
            Center(
              child: Column(
                mainAxisSize: MainAxisSize.min, // Centers the column vertically
                children: [
                  // Admin Button
                  OutlinedButton.icon(
                    onPressed: () async {
                      await _handleAdminSignIn();
                    },
                    icon: Image.asset(
                      'assets/images/ic_google.png',
                      width: 24,
                      height: 24,
                    ),
                    label: const Text("Continue As Admin"),
                    style: OutlinedButton.styleFrom(
                      padding:
                      const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                      textStyle: const TextStyle(
                        fontFamily: "Poppins",
                        fontWeight: FontWeight.w500,
                        fontSize: 16
                      ),
                    ),
                  ),
                  const SizedBox(height: 10), // Spacing between buttons
                  const Text(
                    "Or",
                    style: TextStyle(
                      fontFamily: "Poppins",
                      fontSize: 14,
                    ),
                  ),
                  const SizedBox(height: 10),
                  // Volunteer Button
                  OutlinedButton.icon(
                    onPressed: () {
                      // TODO: Define volunteer sign-in logic here
                    },
                    icon: Image.asset(
                      'assets/images/ic_google.png',
                      width: 24,
                      height: 24,
                    ),
                    label: const Text("Continue As Volunteer"),
                    style: OutlinedButton.styleFrom(
                      padding:
                      const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                      textStyle: const TextStyle(
                        fontFamily: "Poppins",
                        fontWeight: FontWeight.w500,
                        fontSize: 16
                      ),
                    ),
                  ),
                ],
              ),
            ),
            // Bottom Tagline
            const Positioned(
              bottom: 20,
              left: 0,
              right: 0,
              child: Text(
                "Syncing Teams,\nStreamlining Success",
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontFamily: "Poppins",
                  fontWeight: FontWeight.w400,
                  fontSize: 15,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
