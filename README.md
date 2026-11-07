# TONONKIRA APTM

Application Android offline-first ho an'ny chorale.

## Fonctionnalités incluses dans cette version source
- Bibliothèque locale de chants (titre + paroles), sauvegarde sur l'appareil.
- Import d'image/capture et OCR Latin avec modèle ML Kit embarqué dans l'APK.
- Écran de correction du texte OCR avant enregistrement.
- Projection plein écran, taille de texte réglable.
- Import DOCX avec extraction du texte.
- Ouverture des PDF avec le lecteur PDF installé sur Android (pas encore de lecteur PDF intégré).
- Cible Android 8.0 (API 26) et plus.
- Aucune permission Internet demandée.

## Limites à connaître
- Les images doivent être assez nettes; l'OCR peut confondre des lettres. Relire le texte avant sauvegarde.
- Les fichiers Word `.doc` anciens ne sont pas pris en charge; utiliser `.docx`.
- Le PDF est ouvert dans une application externe. Pour transformer un PDF en paroles, faire une capture de la page puis importer cette image via OCR.
- Le lecteur audio et l'association audio par chant restent à finaliser dans cette première base source; ne pas considérer l'audio comme validé.
- Cette source n'est pas un APK compilé ni testé sur téléphone.

## Build
Ouvrir ce projet dans Android Studio (JDK 17, Android SDK 35), puis lancer `:app:assembleDebug`.
APK attendu: `app/build/outputs/apk/debug/app-debug.apk`.
