/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.batache.songs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;

import java.util.List;

import static androidx.core.graphics.ColorUtils.RGBToXYZ;

/**
 * A class the processes media notifications and extracts the right text and background colors.
 */
public class MediaNotificationProcessor {

  /**
   * The fraction below which we select the vibrant instead of the light/dark vibrant color
   */
  private static final float POPULATION_FRACTION_FOR_MORE_VIBRANT = 1.0f;

  /**
   * Minimum saturation that a muted color must have if there exists if deciding between two
   * colors
   */
  private static final float MIN_SATURATION_WHEN_DECIDING = 0.19f;

  /**
   * Minimum fraction that any color must have to be picked up as a text color
   */
  private static final double MINIMUM_IMAGE_FRACTION = 0.002;

  /**
   * The population fraction to select the dominant color as the text color over a the colored
   * ones.
   */
  private static final float POPULATION_FRACTION_FOR_DOMINANT = 0.01f;

  /**
   * The population fraction to select a white or black color as the background over a color.
   */
  private static final float POPULATION_FRACTION_FOR_WHITE_OR_BLACK = 2.5f;
  private static final float BLACK_MAX_LIGHTNESS = 0.08f;
  private static final float WHITE_MIN_LIGHTNESS = 0.90f;
  private static final int RESIZE_BITMAP_AREA = 150 * 150;
  private final Context context;
  private final Palette.Filter mBlackWhiteFilter = (rgb, hsl) -> !isWhiteOrBlack(hsl);

  private int backgroundColor;
  private int foregroundColor;
  private int primaryTextColor;
  private int secondaryTextColor;

  private Drawable drawable;

  public MediaNotificationProcessor(Context context, Drawable drawable) {
    this.context = context;
    this.drawable = drawable;
    getMediaPalette();
  }

  public MediaNotificationProcessor(Context context, Bitmap bitmap) {
    this.context = context;
    this.drawable = new BitmapDrawable(context.getResources(), bitmap);
    getMediaPalette();
  }

  public MediaNotificationProcessor(Context context) {
    this.context = context;
  }

  public void getPaletteAsync(final OnPaletteLoadedListener onPaletteLoadedListener, Drawable drawable) {
    this.drawable = drawable;
    final Handler handler = new Handler();
    new Thread(new Runnable() {
      @Override
      public void run() {
        getMediaPalette();
        handler.post(new Runnable() {
          @Override
          public void run() {
            onPaletteLoadedListener.onPaletteLoaded(MediaNotificationProcessor.this);
          }
        });
      }
    }).start();

  }

  public void getPaletteAsync(OnPaletteLoadedListener onPaletteLoadedListener, Bitmap bitmap) {
    this.drawable = new BitmapDrawable(context.getResources(), bitmap);
    getPaletteAsync(onPaletteLoadedListener, this.drawable);
  }

  /**
   * Processes a drawable and calculates the appropriate colors that should
   * be used.
   */
  public void getMediaPalette() {
    Bitmap bitmap = null;
    if (drawable != null) {
      int width = drawable.getIntrinsicWidth();
      int height = drawable.getIntrinsicHeight();
      int area = width * height;
      if (area > RESIZE_BITMAP_AREA) {
        double factor = Math.sqrt((float) RESIZE_BITMAP_AREA / area);
        width = (int) (factor * width);
        height = (int) (factor * height);
      }
      bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(bitmap);
      drawable.setBounds(0, 0, width, height);
      drawable.draw(canvas);
      Palette.Builder paletteBuilder = generateArtworkPaletteBuilder(bitmap);
      Palette palette = paletteBuilder.generate();
      Palette.Swatch backgroundSwatch = findBackgroundSwatch(palette);
      backgroundColor = backgroundSwatch.getRgb();
      // we want most of the full region again, slightly shifted to the right
      float textColorStartWidthFraction = 0.4f;
      paletteBuilder.setRegion((int) (bitmap.getWidth() * textColorStartWidthFraction), 0,
          bitmap.getWidth(),
          bitmap.getHeight());
      // We're not filtering on white or black
      if (!isWhiteOrBlack(backgroundSwatch.getHsl())) {
        final float backgroundHue = backgroundSwatch.getHsl()[0];
        paletteBuilder.addFilter((rgb, hsl) -> {
          // at least 10 degrees hue difference
          float diff = Math.abs(hsl[0] - backgroundHue);
          return diff > 10 && diff < 350;
        });
      }
      paletteBuilder.addFilter(mBlackWhiteFilter);
      palette = paletteBuilder.generate();
      foregroundColor = selectForegroundColor(backgroundColor, palette);
    }
  }

  /**
   * Select a foreground color depending on whether the background color is dark or light
   *
   * @param backgroundColor Background color to coordinate with
   * @param palette         Artwork palette, should be obtained from generateArtworkPaletteBuilder
   * @return foreground color
   */
  public static int selectForegroundColor(int backgroundColor, Palette palette) {
    if (ContrastColorUtil.isColorLight(backgroundColor)) {
      return selectForegroundColorForSwatches(palette.getDarkVibrantSwatch(),
          palette.getVibrantSwatch(),
          palette.getDarkMutedSwatch(),
          palette.getMutedSwatch(),
          palette.getDominantSwatch(),
          Color.BLACK);
    } else {
      return selectForegroundColorForSwatches(palette.getLightVibrantSwatch(),
          palette.getVibrantSwatch(),
          palette.getLightMutedSwatch(),
          palette.getMutedSwatch(),
          palette.getDominantSwatch(),
          Color.WHITE);
    }
  }

  private static boolean isColorLight(int backgroundColor) {
    return calculateLuminance(backgroundColor) > 0.5f;
  }

  public boolean isLight() {
    return isColorLight(backgroundColor);
  }

  /**
   * Returns the luminance of a color as a float between {@code 0.0} and {@code 1.0}.
   * <p>Defined as the Y component in the XYZ representation of {@code color}.</p>
   */
  @FloatRange(from = 0.0, to = 1.0)
  private static double calculateLuminance(@ColorInt int color) {
    final double[] result = getTempDouble3Array();
    colorToXYZ(color, result);
    // Luminance is the Y component
    return result[1] / 100;
  }

  private static final ThreadLocal<double[]> TEMP_ARRAY = new ThreadLocal<>();

  private static double[] getTempDouble3Array() {
    double[] result = TEMP_ARRAY.get();
    if (result == null) {
      result = new double[3];
      TEMP_ARRAY.set(result);
    }
    return result;
  }


  private static void colorToXYZ(@ColorInt int color, @NonNull double[] outXyz) {
    RGBToXYZ(Color.red(color), Color.green(color), Color.blue(color), outXyz);
  }

  private static int selectForegroundColorForSwatches(Palette.Swatch moreVibrant,
                                                      Palette.Swatch vibrant, Palette.Swatch moreMutedSwatch, Palette.Swatch mutedSwatch,
                                                      Palette.Swatch dominantSwatch, int fallbackColor) {
    Palette.Swatch coloredCandidate = selectVibrantCandidate(moreVibrant, vibrant);
    if (coloredCandidate == null) {
      coloredCandidate = selectMutedCandidate(mutedSwatch, moreMutedSwatch);
    }
    if (coloredCandidate != null) {
      if (dominantSwatch == coloredCandidate) {
        return coloredCandidate.getRgb();
      } else if ((float) coloredCandidate.getPopulation() / dominantSwatch.getPopulation()
          < POPULATION_FRACTION_FOR_DOMINANT
          && dominantSwatch.getHsl()[1] > MIN_SATURATION_WHEN_DECIDING) {
        return dominantSwatch.getRgb();
      } else {
        return coloredCandidate.getRgb();
      }
    } else if (hasEnoughPopulation(dominantSwatch)) {
      return dominantSwatch.getRgb();
    } else {
      return fallbackColor;
    }
  }

  private static Palette.Swatch selectMutedCandidate(Palette.Swatch first,
                                                     Palette.Swatch second) {
    boolean firstValid = hasEnoughPopulation(first);
    boolean secondValid = hasEnoughPopulation(second);
    if (firstValid && secondValid) {
      float firstSaturation = first.getHsl()[1];
      float secondSaturation = second.getHsl()[1];
      float populationFraction = first.getPopulation() / (float) second.getPopulation();
      if (firstSaturation * populationFraction > secondSaturation) {
        return first;
      } else {
        return second;
      }
    } else if (firstValid) {
      return first;
    } else if (secondValid) {
      return second;
    }
    return null;
  }

  private static Palette.Swatch selectVibrantCandidate(Palette.Swatch first,
                                                       Palette.Swatch second) {
    boolean firstValid = hasEnoughPopulation(first);
    boolean secondValid = hasEnoughPopulation(second);
    if (firstValid && secondValid) {
      int firstPopulation = first.getPopulation();
      int secondPopulation = second.getPopulation();
      if (firstPopulation / (float) secondPopulation
          < POPULATION_FRACTION_FOR_MORE_VIBRANT) {
        return second;
      } else {
        return first;
      }
    } else if (firstValid) {
      return first;
    } else if (secondValid) {
      return second;
    }
    return null;
  }

  private static boolean hasEnoughPopulation(Palette.Swatch swatch) {
    // We want a fraction that is at least 1% of the image
    return swatch != null
        && (swatch.getPopulation() / (float) RESIZE_BITMAP_AREA > MINIMUM_IMAGE_FRACTION);
  }

  /**
   * Finds an appropriate background swatch from media artwork.
   *
   * @param artwork Media artwork
   * @return Swatch that should be used as the background of the media notification.
   */
  public static Palette.Swatch findBackgroundSwatch(Bitmap artwork) {
    return findBackgroundSwatch(generateArtworkPaletteBuilder(artwork).generate());
  }

  /**
   * Finds an appropriate background swatch from the palette of media artwork.
   *
   * @param palette Artwork palette, should be obtained from generateArtworkPaletteBuilder
   * @return Swatch that should be used as the background of the media notification.
   */
  public static Palette.Swatch findBackgroundSwatch(Palette palette) {
    // by default we use the dominant palette
    Palette.Swatch dominantSwatch = palette.getDominantSwatch();
    if (dominantSwatch == null) {
      return new Palette.Swatch(Color.WHITE, 100);
    }
    if (!isWhiteOrBlack(dominantSwatch.getHsl())) {
      return dominantSwatch;
    }
    // Oh well, we selected black or white. Lets look at the second color!
    List<Palette.Swatch> swatches = palette.getSwatches();
    float highestNonWhitePopulation = -1;
    Palette.Swatch second = null;
    for (Palette.Swatch swatch : swatches) {
      if (swatch != dominantSwatch
          && swatch.getPopulation() > highestNonWhitePopulation
          && !isWhiteOrBlack(swatch.getHsl())) {
        second = swatch;
        highestNonWhitePopulation = swatch.getPopulation();
      }
    }
    if (second == null) {
      return dominantSwatch;
    }
    if (dominantSwatch.getPopulation() / highestNonWhitePopulation
        > POPULATION_FRACTION_FOR_WHITE_OR_BLACK) {
      // The dominant swatch is very dominant, lets take it!
      // We're not filtering on white or black
      return dominantSwatch;
    } else {
      return second;
    }
  }

  /**
   * Generate a palette builder for media artwork.
   * <p>
   * For producing a smooth background transition, the palette is extracted from only the left
   * side of the artwork.
   *
   * @param artwork Media artwork
   * @return Builder that generates the {@link Palette} for the media artwork.
   */
  public static Palette.Builder generateArtworkPaletteBuilder(Bitmap artwork) {
    // for the background we only take the left side of the image to ensure
    // a smooth transition
    return Palette.from(artwork)
        .setRegion(0, 0, artwork.getWidth() / 2, artwork.getHeight())
        .clearFilters() // we want all colors, red / white / black ones too!
        .resizeBitmapArea(RESIZE_BITMAP_AREA);
  }

  private static boolean isWhiteOrBlack(float[] hsl) {
    return isBlack(hsl) || isWhite(hsl);
  }

  /**
   * @return true if the color represents a color which is close to black.
   */
  private static boolean isBlack(float[] hslColor) {
    return hslColor[2] <= BLACK_MAX_LIGHTNESS;
  }

  /**
   * @return true if the color represents a color which is close to white.
   */
  private static boolean isWhite(float[] hslColor) {
    return hslColor[2] >= WHITE_MIN_LIGHTNESS;
  }


  /**
   * The lightness difference that has to be added to the primary text color to obtain the
   * secondary text color when the background is light.
   */
  private static final int LIGHTNESS_TEXT_DIFFERENCE_LIGHT = 20;

  /**
   * The lightness difference that has to be added to the primary text color to obtain the
   * secondary text color when the background is dark.
   * A bit less then the above value, since it looks better on dark backgrounds.
   */
  private static final int LIGHTNESS_TEXT_DIFFERENCE_DARK = -10;

  private void ensureColors(int backgroundColor, int mForegroundColor) {
    double backLum = ContrastColorUtil.calculateLuminance(backgroundColor);
    double textLum = ContrastColorUtil.calculateLuminance(mForegroundColor);
    double contrast = ContrastColorUtil.calculateContrast(mForegroundColor,
        backgroundColor);
    // We only respect the given colors if worst case Black or White still has
    // contrast
    boolean backgroundLight = backLum > textLum
        && ContrastColorUtil.satisfiesTextContrast(backgroundColor, Color.BLACK)
        || backLum <= textLum
        && !ContrastColorUtil.satisfiesTextContrast(backgroundColor, Color.WHITE);
    if (contrast < 4.5f) {
      if (backgroundLight) {
        secondaryTextColor = ContrastColorUtil.findContrastColor(
            mForegroundColor,
            backgroundColor,
            true /* findFG */,
            4.5f);
        primaryTextColor = ContrastColorUtil.changeColorLightness(
            secondaryTextColor, -LIGHTNESS_TEXT_DIFFERENCE_LIGHT);
      } else {
        secondaryTextColor =
            ContrastColorUtil.findContrastColorAgainstDark(
                mForegroundColor,
                backgroundColor,
                true /* findFG */,
                4.5f);
        primaryTextColor = ContrastColorUtil.changeColorLightness(
            secondaryTextColor, -LIGHTNESS_TEXT_DIFFERENCE_DARK);
      }
    } else {
      primaryTextColor = mForegroundColor;
      secondaryTextColor = ContrastColorUtil.changeColorLightness(
          primaryTextColor, backgroundLight ? LIGHTNESS_TEXT_DIFFERENCE_LIGHT
              : LIGHTNESS_TEXT_DIFFERENCE_DARK);
      if (ContrastColorUtil.calculateContrast(secondaryTextColor,
          backgroundColor) < 4.5f) {
        // oh well the secondary is not good enough
        if (backgroundLight) {
          secondaryTextColor = ContrastColorUtil.findContrastColor(
              secondaryTextColor,
              backgroundColor,
              true /* findFG */,
              4.5f);
        } else {
          secondaryTextColor
              = ContrastColorUtil.findContrastColorAgainstDark(
              secondaryTextColor,
              backgroundColor,
              true /* findFG */,
              4.5f);
        }
        primaryTextColor = ContrastColorUtil.changeColorLightness(
            secondaryTextColor, backgroundLight
                ? -LIGHTNESS_TEXT_DIFFERENCE_LIGHT
                : -LIGHTNESS_TEXT_DIFFERENCE_DARK);
      }
    }
  }

  public int getBackgroundColor() {
    return backgroundColor;
  }

  public int getForegroundColor() {
    return foregroundColor;
  }

  public int getPrimaryTextColor() {
    return primaryTextColor;
  }

  public int getSecondaryTextColor() {
    return secondaryTextColor;
  }

  public interface OnPaletteLoadedListener {
    void onPaletteLoaded(MediaNotificationProcessor mediaNotificationProcessor);
  }
}