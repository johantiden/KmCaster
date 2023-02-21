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

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.whitemagicsoftware.kmcaster.listeners.DebugListener;
import com.whitemagicsoftware.kmcaster.listeners.FrameDragListener;
import com.whitemagicsoftware.kmcaster.listeners.KeyboardListener;
import com.whitemagicsoftware.kmcaster.listeners.MouseListener;
import com.whitemagicsoftware.kmcaster.ui.TranslucentPanel;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi.Style;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.github.kwhat.jnativehook.GlobalScreen.*;
import static com.whitemagicsoftware.kmcaster.ui.FontLoader.initFonts;
import static java.lang.Integer.valueOf;
import static java.util.logging.Level.OFF;
import static java.util.logging.Logger.getLogger;
import static javax.swing.SwingUtilities.invokeLater;
import static picocli.CommandLine.Help.ColorScheme;

/**
 * This class is responsible for casting key presses and mouse clicks on the
 * screen. While there is a plethora of software out here that does this,
 * none meet all the following criteria: small size, easily positioned,
 * show single keystroke or chord, shows left/right mouse clicks, shows
 * release of modifier keys when quickly typing, and traps all keys typed
 * from within Sikuli.
 * <p>
 * When using the XFCE Window Manager, enable compositing as follows:
 * </p>
 * <ol>
 *   <li>Open the Xfce Applications Menu.</li>
 *   <li>Click Settings, Settings Editor.</li>
 *   <li>Select xfwm4, near the bottom left.</li>
 *   <li>Check the value column for use_compositing.</li>
 * </ol>
 */
public final class KmCaster extends JFrame {
  private final Settings mUserSettings = new Settings( this );

  /**
   * Constructs a window with the class name for its frame title.
   */
  public KmCaster() {
    super( KmCaster.class.getSimpleName() );
  }

  public void init() {
    final var hardwareImages = new HardwareImages( mUserSettings );
    final var eventHandler = new EventHandler( hardwareImages, mUserSettings );

    initWindowFrame();
    initWindowContents( hardwareImages );
    pack();
    setResizable( false );
    initListeners( eventHandler );
    setVisible( true );
  }

  private void initWindowFrame() {
    setDefaultCloseOperation( EXIT_ON_CLOSE );
    setLocationRelativeTo( null );
    setUndecorated( true );
    setAlwaysOnTop( true );
    setBackground( getUserBgColour() );

    // Prevent tabbing to non-existent components.
    setFocusTraversalKeysEnabled( false );
  }

  private void initWindowContents( final HardwareImages hardwareImages ) {
    final var hgap = getGapHorizontal();
    final var vgap = getGapVertical();
    final var panel = new TranslucentPanel( hgap, vgap );

    for( final var hwSwitch : HardwareSwitch.values() ) {
      final var component = hardwareImages.get( hwSwitch );

      // If there is no image for the switch, it may be a mouse button without
      // a direct visual representation.
      if( component != null ) {
        panel.add( component );
      }
    }

    getContentPane().add( panel );
  }

  private void initListeners( final EventHandler eventHandler ) {
    initWindowDragListener( this );
    initMouseListener( eventHandler );
    initKeyboardListener( eventHandler );
    initDebugListener();
  }

  private void initWindowDragListener( final JFrame listener ) {
    final var frameDragListener = new FrameDragListener( listener );
    addMouseListener( frameDragListener );
    addMouseMotionListener( frameDragListener );
  }

  private void initMouseListener( final PropertyChangeListener listener ) {
    final MouseListener mouseListener = new MouseListener();
    addNativeMouseListener( mouseListener );
    addNativeMouseMotionListener( mouseListener );
    addNativeMouseWheelListener( mouseListener );
    mouseListener.addPropertyChangeListener( listener );
  }

  private void initKeyboardListener( final PropertyChangeListener listener ) {
    final KeyboardListener keyboardListener = new KeyboardListener( getUserSettings() );
    addNativeKeyListener( keyboardListener );
    keyboardListener.addPropertyChangeListener( listener );
    keyboardListener.initModifiers();
  }

  private void initDebugListener() {
    if( isDebugEnabled() ) {
      final DebugListener debugListener = new DebugListener();
      addNativeKeyListener( debugListener );
    }
  }

  @SuppressWarnings( "PointlessArithmeticExpression" )
  private Color getUserBgColour() {
    final var hex = getUserSettings().getBackgroundColour();
    final var index = hex.startsWith( "#" ) ? 1 : 0;

    try {
      final var r = valueOf( hex.substring( index + 0, index + 2 ), 16 );
      final var g = valueOf( hex.substring( index + 2, index + 4 ), 16 );
      final var b = valueOf( hex.substring( index + 4, index + 6 ), 16 );
      final var a = valueOf( hex.substring( index + 6, index + 8 ), 16 );

      return new Color( r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f );
    } catch( final Exception e ) {
      e.printStackTrace();
    }

    return new Color( .2f, .2f, .2f, .5f );
  }

  private int getGapHorizontal() {
    return getUserSettings().getGapHorizontal();
  }

  private int getGapVertical() {
    return getUserSettings().getGapVertical();
  }

  private boolean isDebugEnabled() {
    return getUserSettings().isDebugEnabled();
  }

  private Settings getUserSettings() {
    return mUserSettings;
  }

  /**
   * Suppress writing logging messages to standard output.
   */
  private static void disableNativeHookLogger() {
    final var logger = getLogger( GlobalScreen.class.getPackage().getName() );
    logger.setLevel( OFF );
    logger.setUseParentHandlers( false );
  }

  private static ColorScheme createColourScheme() {
    return new ColorScheme.Builder()
      .commands( Style.bold )
      .options( Style.fg_blue, Style.bold )
      .parameters( Style.fg_blue )
      .optionParams( Style.italic )
      .errors( Style.fg_red, Style.bold )
      .stackTraces( Style.italic )
      .build();
  }

  /**
   * Main entry point.
   *
   * @param args Unused.
   */
  public static void main( final String[] args )
    throws NativeHookException, IOException, URISyntaxException {
    initFonts();
    disableNativeHookLogger();
    registerNativeHook();

    while( !isNativeHookRegistered() ) {
      Thread.yield();
    }

    final var kc = new KmCaster();
    final var parser = new CommandLine( kc.getUserSettings() );
    parser.setColorScheme( createColourScheme() );

    invokeLater( () -> {
      final var exitCode = parser.execute( args );
      final var parseResult = parser.getParseResult();

      if( parseResult.isUsageHelpRequested() ) {
        System.exit( exitCode );
      }
    } );
  }
}
