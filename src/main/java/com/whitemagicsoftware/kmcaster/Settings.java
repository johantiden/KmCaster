/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.whitemagicsoftware.kmcaster;

import picocli.CommandLine;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.awt.Font.*;
import static java.util.Map.entry;
import static javax.swing.SwingUtilities.invokeLater;

@CommandLine.Command(
  name = "KmCaster",
  mixinStandardHelpOptions = true,
  description = "Displays key presses and mouse clicks on the screen."
)
@SuppressWarnings( {"FieldMayBeFinal", "CanBeFinal"} )
public final class Settings implements Callable<Integer> {
  /**
   * Minimum application height, in pixels.
   */
  private static final int MIN_HEIGHT_PX = 20;

  /**
   * Maps font styles to {@link Font} API equivalents.
   */
  private static Map<String, Integer> FONT_STYLES = Map.ofEntries(
    entry( "plain", PLAIN ),
    entry( "bold", BOLD ),
    entry( "italic", ITALIC ),
    entry( "bold+italic", BOLD + ITALIC )
  );

  /**
   * Executable class.
   */
  private final KmCaster mKmCaster;

  /**
   * Milliseconds to wait before releasing (clearing) a regular key.
   */
  @CommandLine.Option(
    names = {"-a", "--delay-alphanum"},
    description =
      "Regular key release delay (${DEFAULT-VALUE} milliseconds)",
    paramLabel = "ms",
    defaultValue = "250"
  )
  private int mDelayKeyRegular = 250;

  /**
   * Milliseconds to wait before releasing (clearing) a mouse button.
   */
  @CommandLine.Option(
    names = {"-b", "--delay-button"},
    description =
      "Mouse button release delay (${DEFAULT-VALUE} milliseconds)",
    paramLabel = "ms",
    defaultValue = "100"
  )
  private int mDelayMouseButton = 100;

  /**
   * Background colour.
   */
  @CommandLine.Option(
    names = {"-c", "--colour"},
    description =
      "Background colour, including alpha transparency (${DEFAULT-VALUE} RGBA)",
    paramLabel = "hex",
    defaultValue = "30303077"
  )
  private String mBackgroundColour = "30303077";

  /**
   * Debugging for keystrokes.
   */
  @CommandLine.Option(
    names = {"-d", "--debug"},
    description =
      "Enable debugging (${DEFAULT-VALUE})",
    paramLabel = "Boolean",
    defaultValue = "false"
  )
  private boolean mDebug = false;

  /**
   * Application preferred font name.
   */
  @CommandLine.Option(
    names = {"-f", "--font-name"},
    description =
      "Font name (${DEFAULT-VALUE})",
    paramLabel = "string",
    defaultValue = "Inter"
  )
  private String mFontName = "Inter";

  /**
   * Application preferred font style.
   */
  @CommandLine.Option(
    names = {"--font-style"},
    description =
      "plain, bold, italic, bold+italic (${DEFAULT-VALUE})",
    paramLabel = "string",
    defaultValue = "bold"
  )
  private String mFontStyle = "bold";

  /**
   * Amount of padding above and below the frame.
   */
  @CommandLine.Option(
    names = {"--gap-horizontal"},
    description =
      "Amount of padding above and below frame (${DEFAULT-VALUE} pixels)",
    paramLabel = "pixels",
    defaultValue = "5"
  )
  private int mGapHorizontal = 5;

  /**
   * Amount of padding between items in the frame.
   */
  @CommandLine.Option(
    names = {"--gap-vertical"},
    description =
      "Amount of padding between switches (${DEFAULT-VALUE} pixels)",
    paramLabel = "pixels",
    defaultValue = "5"
  )
  private int mGapVertical = 5;

  /**
   * Number of times to count a key press before displaying +.
   */
  @CommandLine.Option(
    names = {"-k", "--key-counter"},
    description =
      "Count repeated key presses (${DEFAULT-VALUE} times)",
    paramLabel = "number",
    defaultValue = "9"
  )
  private int mKeyCount = 9;

  /**
   * Milliseconds to wait before releasing (clearing) any modifier key.
   */
  @CommandLine.Option(
    names = {"-m", "--delay-modifier"},
    description =
      "Modifier key release delay (${DEFAULT-VALUE} milliseconds)",
    paramLabel = "ms",
    defaultValue = "150"
  )
  private int mDelayKeyModifier = 150;

  /**
   * Application height in pixels. Images are scaled to this height, maintaining
   * aspect ratio. The height constrains the width, so as long as the width
   * is large enough, the application's window will adjust to fit.
   */
  @CommandLine.Option(
    names = {"-p", "--proportion"},
    description =
      "Application height (${DEFAULT-VALUE} pixels)",
    paramLabel = "pixels",
    defaultValue = "100"
  )
  private int mHeight = 100;

  /**
   * Milliseconds to wait before releasing (clearing) a mouse scroll event.
   */
  @CommandLine.Option(
    names = {"-s", "--delay-scroll"},
    description =
      "Mouse scroll release delay (${DEFAULT-VALUE} milliseconds)",
    paramLabel = "ms",
    defaultValue = "300"
  )
  private int mDelayMouseScroll = 300;

  /**
   * Show the Super key as a modifier
   */
  @CommandLine.Option(
          names = {"-S", "--super"},
          description =
                  "Show the Super key as a modifier (${DEFAULT-VALUE})",
          paramLabel = "Boolean",
          defaultValue = "false"
  )
  private boolean mSuper = false;

  public Settings( final KmCaster kmCaster ) {
    assert kmCaster != null;

    mKmCaster = kmCaster;
  }


  /**
   * Invoked after the command-line arguments are parsed to launch the
   * application.
   *
   * @return Exit level zero.
   */
  @Override
  public Integer call() {
    invokeLater( mKmCaster::init );
    return 0;
  }

  public int getDelayKeyRegular() {
    return mDelayKeyRegular;
  }

  public int getDelayKeyModifier() {
    return mDelayKeyModifier;
  }

  public int getDelayMouseButton() {
    return mDelayMouseButton;
  }

  public int getDelayMouseScroll() {
    return mDelayMouseScroll;
  }

  public int getKeyCount() {
    return mKeyCount < 2 ? 2 : mKeyCount;
  }

  @SuppressWarnings( "MagicConstant" )
  public Font createFont() {
    final var style = FONT_STYLES.getOrDefault(
      mFontStyle.toLowerCase(), BOLD
    );

    return new Font( mFontName, style, 100 );
  }

  public Dimension createAppDimensions() {
    return new Dimension( 1024 + getHeight(), getHeight() );
  }

  /**
   * This will return the user-specified height
   *
   * @return The application height, in pixels.
   */
  private int getHeight() {
    return mHeight < MIN_HEIGHT_PX ? MIN_HEIGHT_PX : mHeight;
  }

  public int getGapHorizontal() {
    return mGapHorizontal;
  }

  public int getGapVertical() {
    return mGapVertical;
  }

  public String getBackgroundColour() {
    return mBackgroundColour;
  }

  public boolean isDebugEnabled() {
    return mDebug;
  }

  public boolean isSuperEnabled() {
    return mSuper;
  }
}
