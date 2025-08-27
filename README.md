📷 Card Number Scanning
- This project implements card number scanning using 3 libraries:
1) Card.io
   - old but still effective
   - works well with embossed cards
   - slower and less reliable for flat-printed card numbers
  
2) ProcessOut Android SDK
   - modern, actively maintained
   - easy to integrate with POCardScannerLauncher
   - performance similar to ML Kit - struggles with embossed/flat white card
  
3) Google ML Kit Text Recognition
   - general-purpose OCR library
   - flexible (can be used for other text recognition tasks)
   - requires custom logic to detect/extract only card number / ui
   - accuracy depends on card contrast and lighting
