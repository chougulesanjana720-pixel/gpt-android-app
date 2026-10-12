import os
import unittest
import numpy as np
from PIL import Image, ImageDraw
from image_analyzer import ImageAnalyzer

class TestImageAnalyzer(unittest.TestCase):

    def setUp(self):
        # Create image 1: Synthetic Chart/Plot (Blue background, white rectangle lines)
        self.img1_path = "test_chart.png"
        img1 = Image.new("RGB", (400, 300), color=(20, 50, 200))
        draw1 = ImageDraw.Draw(img1)
        draw1.rectangle([50, 50, 350, 250], outline=(255, 255, 255), width=3)
        draw1.line([50, 250, 350, 50], fill=(255, 200, 0), width=4)
        img1.save(self.img1_path)

        # Create image 2: Synthetic Diagram/Tabular (High brightness, green/red boxes)
        self.img2_path = "test_diagram.png"
        img2 = Image.new("RGB", (600, 600), color=(245, 245, 245))
        draw2 = ImageDraw.Draw(img2)
        draw2.rectangle([20, 20, 280, 280], fill=(0, 180, 50), outline=(0, 0, 0))
        draw2.rectangle([300, 300, 580, 580], fill=(220, 40, 40), outline=(0, 0, 0))
        img2.save(self.img2_path)

    def tearDown(self):
        if os.path.exists(self.img1_path):
            os.remove(self.img1_path)
        if os.path.exists(self.img2_path):
            os.remove(self.img2_path)

    def test_raw_data_extraction_differs_per_image(self):
        analyzer1 = ImageAnalyzer(self.img1_path)
        raw_data1 = analyzer1.extract_raw_data()

        analyzer2 = ImageAnalyzer(self.img2_path)
        raw_data2 = analyzer2.extract_raw_data()

        # Verify raw data dimensions are different
        self.assertEqual(raw_data1["dimensions"]["width"], 400)
        self.assertEqual(raw_data1["dimensions"]["height"], 300)
        self.assertEqual(raw_data2["dimensions"]["width"], 600)
        self.assertEqual(raw_data2["dimensions"]["height"], 600)

        # Verify color channel metrics are different
        self.assertNotEqual(raw_data1["channel_means"]["blue"], raw_data2["channel_means"]["blue"])

        # Verify responses are dynamic and unique per image
        resp1 = analyzer1.generate_response("Explain this plot")
        resp2 = analyzer2.generate_response("Explain this diagram")

        self.assertIn("400 x 300 px", resp1)
        self.assertIn("600 x 600 px", resp2)
        self.assertNotEqual(resp1, resp2)
        print("\n--- TEST RESPONSE 1 ---")
        print(resp1)
        print("\n--- TEST RESPONSE 2 ---")
        print(resp2)

if __name__ == '__main__':
    unittest.main()
