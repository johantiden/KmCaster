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

import com.whitemagicsoftware.kmcaster.ui.DimensionTuple;
import com.whitemagicsoftware.kmcaster.ui.PaddedInsets;
import com.whitemagicsoftware.kmcaster.util.Pair;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static com.whitemagicsoftware.kmcaster.HardwareState.SWITCH_PRESSED;
import static com.whitemagicsoftware.kmcaster.HardwareState.SWITCH_RELEASED;
import static com.whitemagicsoftware.kmcaster.HardwareSwitch.*;
import static com.whitemagicsoftware.kmcaster.exceptions.Rethrowable.rethrow;
import static java.lang.String.format;

/**
 * Responsible for loading vector graphics representations of application
 * images. The images provide an on-screen interface that indicate to the user
 * what key or mouse events have been triggered.
 */
public final class HardwareImages {
  private final static String DIR_IMAGES = "/images";
  private final static String DIR_IMAGES_KEYBOARD = DIR_IMAGES + "/key";
  private final static String DIR_IMAGES_MOUSE = DIR_IMAGES + "/mouse";

  private final static Map<HardwareSwitch, String> FILE_NAME_PREFIXES = Map.of(
      KEY_ALT, "medium",
      KEY_CTRL, "medium",
      KEY_SHIFT, "shift",
      KEY_SUPER, "short",
      KEY_REGULAR, "short"
  );

  /**
   * Defines the amount of space between around the vector graphic projection
   * of a key. These values are specific to the projected sizes and must be
   * measured while editing the vector graphic (e.g., using the ruler tool),
   * rounded up then codified here. The insets will be scaled to fit the
   * application window frame.
   * <p>
   * The shift key insets offset the safe area to the right of the up arrow
   * icon.
   * </p>
   */
  private final static Map<HardwareSwitch, Insets> SWITCH_INSETS = Map.of(
      KEY_ALT, new Insets( 10, 11, 12, 11 ),
      KEY_CTRL, new Insets( 10, 11, 12, 11 ),
      KEY_SHIFT, new Insets( 10, 50, 12, 11 ),
      KEY_SUPER, new Insets( 10, 11, 12, 11 ),
      KEY_REGULAR, new Insets( 3, 7, 6, 7 ),
      MOUSE_EXTRA, new Insets( 27, 5, 11, 5 )
  );

  private final static SvgRasterizer sRasterizer = new SvgRasterizer();

  private final Dimension mAppDimensions;

  private final Map
      <HardwareSwitch, HardwareComponent<HardwareSwitchState, Image>>
      mSwitches = new HashMap<>();

  public HardwareImages( final Settings userSettings ) {
    mAppDimensions = userSettings.createAppDimensions();

    final var mouseReleased = mouseImage( "0" );
    final var mouseScale = mouseReleased.getValue();
    final var mouseStates =
        createHardwareComponent( MOUSE_EXTRA, mouseScale );

    for( final var hwSwitch : mouseSwitches() ) {
      final var stateOn = state( hwSwitch, SWITCH_PRESSED );
      final var stateOff = state( hwSwitch, SWITCH_RELEASED );
      final var imageDn = mouseImage( hwSwitch.toString() );

      mouseStates.put( stateOn, imageDn.getKey() );
      mouseStates.put( stateOff, mouseReleased.getKey() );
      mSwitches.put( hwSwitch, mouseStates );
    }

    for( final var key : keyboardSwitches( userSettings.isSuperEnabled() ) ) {
      final var stateOn = state( key, SWITCH_PRESSED );
      final var stateOff = state( key, SWITCH_RELEASED );
      final var imageDn = keyDnImage( FILE_NAME_PREFIXES.get( key ) );
      final var imageUp = keyUpImage( FILE_NAME_PREFIXES.get( key ) );
      final var scale = imageDn.getValue();
      final var keyStates = createHardwareComponent( key, scale );

      keyStates.put( stateOn, imageDn.getKey() );
      keyStates.put( stateOff, imageUp.getKey() );
      mSwitches.put( key, keyStates );
    }
  }

  private PaddedInsets createInsets( final HardwareSwitch hwSwitch ) {
    return new PaddedInsets( SWITCH_INSETS.get( hwSwitch ) );
  }

  private HardwareComponent<HardwareSwitchState, Image> createHardwareComponent(
      final HardwareSwitch hwSwitch,
      final DimensionTuple scale ) {
    final var insets = createInsets( hwSwitch );
    final var scaledInsets = insets.scale( scale );

    return new HardwareComponent<>( scaledInsets );
  }

  public HardwareComponent<HardwareSwitchState, Image> get(
      final HardwareSwitch hwSwitch ) {
    return mSwitches.get( hwSwitch );
  }

  private HardwareSwitchState state(
      final HardwareSwitch name, final HardwareState state ) {
    return new HardwareSwitchState( name, state );
  }

  private Pair<Image, DimensionTuple> mouseImage( final String prefix ) {
    return createImage( format( "%s/%s", DIR_IMAGES_MOUSE, prefix ) );
  }

  private Pair<Image, DimensionTuple> keyImage(
      final String state, final String prefix ) {
    return createImage(
        format( "%s/%s/%s", DIR_IMAGES_KEYBOARD, state, prefix )
    );
  }

  private Pair<Image, DimensionTuple> keyUpImage( final String prefix ) {
    return keyImage( "up", prefix );
  }

  private Pair<Image, DimensionTuple> keyDnImage( final String prefix ) {
    return keyImage( "dn", prefix );
  }

  private Pair<Image, DimensionTuple> createImage( final String path ) {
    final var resource = format( "%s.svg", path );

    try {
      final var d = sRasterizer.loadDiagram( resource );
      final var scale = sRasterizer.calculateScale( d, getAppDimensions() );
      final var image = sRasterizer.rasterize( d, getAppDimensions() );

      return new Pair<>( image, scale );
    } catch( final Exception ex ) {
      rethrow( ex );
    }

    final var msg = format( "Missing resource %s", resource );
    throw new RuntimeException( msg );
  }

  private Dimension getAppDimensions() {
    return mAppDimensions;
  }
}
