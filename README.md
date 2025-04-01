Video and Image Processing Tool
==============================

Overview
--------
This Java application processes images and videos to create a compiled video output. It can:
- Sort media files by creation date
- Convert images to video clips
- Generate audio descriptions for media using AI
- Create video collages
- Generate custom postcard images
- Concatenate all processed media into a final video

Features
--------
Core Functionalities:
- File Organization: Sorts media files by metadata (creation date)
- Media Processing: Converts images to videos and normalizes video formats
- AI Integration: Uses OpenAI APIs for image generation and audio description
- Video Editing: Creates collages, overlays images, and concatenates videos

Key Components:
1. FileOrganizer.java - Handles file operations and command execution
2. MakeVideo.java - Contains all video processing and editing functions
3. ExifFunctions.java - Extracts and processes metadata from media files
4. IaFunctions.java - Interfaces with AI services for image and audio generation
5. Main.java - The entry point that orchestrates the entire process

Requirements
------------
- Java 8 or higher
- FFmpeg installed and in system PATH
- ExifTool installed (for metadata extraction)
- OpenAI API key (set in ChatGPTKey class)
- Internet connection (for AI services)

Installation
------------
1. Clone or download the repository
2. Ensure FFmpeg and ExifTool are installed and accessible
3. Set your OpenAI API key in the ChatGPTKey class
4. Compile all Java files

Usage
-----
Run the program with:
java Main

When prompted:
1. Enter the path to your media files (images/videos)
2. Enter the desired mood/theme for the generated postcard

The program will:
1. Scan and sort your media files
2. Process each file (convert images to videos, generate audio)
3. Create a collage of all media
4. Generate a postcard based on your mood input
5. Combine everything into a final video output

Output Files
------------
The program creates several temporary files during processing and cleans them up automatically. The final outputs are:
- PostalCard1.png: Generated postcard image
- output2.mp4: Chronological video of all media
- output3.mp4: Collage video of all media
- output4.mp4: Final combined video (postcard + chronological + collage)

Configuration
-------------
You can modify these parameters in Main.java:
- Video dimensions (width/height)
- Output filenames
- Processing options

Notes
-----
- The program requires significant processing time for large media collections
- Internet connectivity is required for AI services
- Ensure you have sufficient disk space for temporary files
- The OpenAI API may incur costs depending on usage

License
-------
This project is provided as-is without warranty. Users are responsible for complying with OpenAI's terms of service when using this software.