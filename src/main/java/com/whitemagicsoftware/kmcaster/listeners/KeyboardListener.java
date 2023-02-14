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
package com.whitemagicsoftware.kmcaster.listeners;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.whitemagicsoftware.kmcaster.HardwareSwitch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.whitemagicsoftware.kmcaster.HardwareSwitch.*;
import static com.whitemagicsoftware.kmcaster.listeners.KeyboardListener.HandedSwitch.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Map.entry;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

/**
 * Responsible for sending property change events for keyboard state changes.
 */
public final class KeyboardListener
  extends PropertyDispatcher<HardwareSwitch>
  implements NativeKeyListener {
  private final static String KEY_SPACE = "Space";
  private final static String KEY_BACKSPACE = "Back ⌫";
  private final static String KEY_TAB = "Tab ↔";
  private final static String KEY_ENTER = "Enter ⏎";
  private static final String KEY_ESCAPE = "Esc";

  private final static Map<Character, String> CHAR_CODES =
    Map.ofEntries(
      entry( '\b', KEY_BACKSPACE ),
      entry( '\t', KEY_TAB ),
      entry( '\r', KEY_ENTER ),
      entry( '\u001B', KEY_ESCAPE ),
      entry( ' ', KEY_SPACE )
    );

  /**
   * Shortens text strings from the keyboard library to fit the UI key.
   */
  private static final Map<String, String> TRANSLATE = Map.ofEntries(
    entry( "Caps Lock", "Caps" ),
    entry( "Num Lock", "Num" ),
    entry( "Scroll Lock", "Scrl" ),
    entry( "Print Screen", "Print" ),
    entry( "Up", "↑" ),
    entry( "Down", "↓" ),
    entry( "Left", "←" ),
    entry( "Right", "→" )
  );

  /**
   * The key is the raw key code return from the {@link NativeKeyEvent}, the
   * value is the human-readable text to display on screen.
   */
  @SuppressWarnings( "JavacQuirks" )
  private final static Map<Integer, String> RAW_CODES =
    Map.ofEntries(
      entry( 8, KEY_BACKSPACE ),
      entry( 9, KEY_TAB ),
      entry( 13, KEY_ENTER ),
      entry( 27, KEY_ESCAPE ),
      entry( 32, KEY_SPACE ),
      entry( 33, "!" ),
      entry( 34, "\"" ),
      entry( 35, "#" ),
      entry( 36, "$" ),
      entry( 37, "%" ),
      entry( 38, "&" ),
      entry( 39, "'" ),
      entry( 40, "(" ),
      entry( 41, ")" ),
      entry( 42, "*" ),
      entry( 43, "+" ),
      entry( 44, "," ),
      entry( 45, "-" ),
      entry( 46, "." ),
      entry( 47, "/" ),
      entry( 58, ":" ),
      entry( 59, ";" ),
      entry( 60, "<" ),
      entry( 61, "=" ),
      entry( 62, ">" ),
      entry( 63, "?" ),
      entry( 64, "@" ),
      entry( 91, "[" ),
      entry( 92, "\\" ),
      entry( 93, "]" ),
      entry( 94, "^" ),
      entry( 95, "_" ),
      entry( 96, "`" ),
      entry( 97, "a" ),
      entry( 98, "b" ),
      entry( 99, "c" ),
      entry( 100, "d" ),
      entry( 101, "e" ),
      entry( 102, "f" ),
      entry( 103, "g" ),
      entry( 104, "h" ),
      entry( 105, "i" ),
      entry( 106, "j" ),
      entry( 107, "k" ),
      entry( 108, "l" ),
      entry( 109, "m" ),
      entry( 110, "n" ),
      entry( 111, "o" ),
      entry( 112, "p" ),
      entry( 113, "q" ),
      entry( 114, "r" ),
      entry( 115, "s" ),
      entry( 116, "t" ),
      entry( 117, "u" ),
      entry( 118, "v" ),
      entry( 119, "w" ),
      entry( 120, "x" ),
      entry( 121, "y" ),
      entry( 122, "z" ),
      entry( 123, "{" ),
      entry( 124, "|" ),
      entry( 125, "}" ),
      entry( 126, "~" ),
      entry( 65056, KEY_TAB ),
      entry( 65288, KEY_BACKSPACE ),
      entry( 65289, KEY_TAB ),
      entry( 65293, KEY_ENTER ),
      entry( 65299, "Pause" ),
      entry( 65301, "SysRq" ),
      entry( 65307, "Esc" ),
      entry( 65360, "Home" ),
      entry( 65361, "←" ),
      entry( 65362, "↑" ),
      entry( 65363, "→" ),
      entry( 65364, "↓" ),
      entry( 65365, "PgUp" ),
      entry( 65366, "PgDn" ),
      entry( 65367, "End" ),
      entry( 65377, "Print" ),
      entry( 65379, "Ins" ),
      entry( 65387, "Break" ),
      entry( 65535, "Del" ),
      entry( 65506, "Shift" ),
      entry( 65407, "Num" ),
      entry( 65421, "Num ⏎" ),
      entry( 65430, "Num ←" ),
      entry( 65431, "Num ↑" ),
      entry( 65432, "Num →" ),
      entry( 65433, "Num ↓" ),
      entry( 65429, "Num Home" ),
      entry( 65434, "Num PgUp" ),
      entry( 65435, "Num PgDn" ),
      entry( 65436, "Num End" ),
      entry( 65437, "Num Clear" ),
      entry( 65438, "Num Ins" ),
      entry( 65439, "Num Del" ),
      entry( 65450, "Num *" ),
      entry( 65451, "Num +" ),
      entry( 65452, "Num Sep" ),
      entry( 65453, "Num -" ),
      entry( 65454, "Num ." ),
      entry( 65455, "Num /" ),
      entry( 65456, "Num 0" ),
      entry( 65457, "Num 1" ),
      entry( 65458, "Num 2" ),
      entry( 65459, "Num 3" ),
      entry( 65460, "Num 4" ),
      entry( 65461, "Num 5" ),
      entry( 65462, "Num 6" ),
      entry( 65463, "Num 7" ),
      entry( 65464, "Num 8" ),
      entry( 65465, "Num 9" ),
      entry( 65470, "F1" ),
      entry( 65471, "F2" ),
      entry( 65472, "F3" ),
      entry( 65473, "F4" ),
      entry( 65474, "F5" ),
      entry( 65475, "F6" ),
      entry( 65476, "F7" ),
      entry( 65477, "F8" ),
      entry( 65478, "F9" ),
      entry( 65479, "F10" ),
      entry( 65480, "F11" ),
      entry( 65481, "F12" ),
      entry( 65300, "Scrl" ),
      entry( 65509, "Caps" )
    );

  /**
   * Maps left and right switches to their on-screen representation. This
   * allows the left and right keys to control whether the switch is active,
   * independently.
   */
  enum HandedSwitch {
    KEY_SHIFT_LEFT( KEY_SHIFT ),
    KEY_SHIFT_RIGHT( KEY_SHIFT ),
    KEY_SUPER_LEFT( KEY_SUPER ),
    KEY_SUPER_RIGHT( KEY_SUPER ),
    KEY_CTRL_LEFT( KEY_CTRL ),
    KEY_CTRL_RIGHT( KEY_CTRL ),
    KEY_ALT_LEFT( KEY_ALT ),
    KEY_ALT_RIGHT( KEY_ALT );

    final HardwareSwitch mHwSwitch;

    HandedSwitch( final HardwareSwitch hwSwitch ) {
      assert hwSwitch != null;
      mHwSwitch = hwSwitch;
    }

    public HardwareSwitch getHardwareSwitch() {
      return mHwSwitch;
    }
  }

  /**
   * The key is the raw key code return from the {@link NativeKeyEvent}, the
   * value is the human-readable text to display on screen.
   */
  private final static Map<Integer, HandedSwitch> MODIFIERS_WINDOWS =
    Map.ofEntries(
      entry( 160, KEY_SHIFT_LEFT ),
      entry( 161, KEY_SHIFT_RIGHT ),
      entry( 162, KEY_CTRL_LEFT ),
      entry( 163, KEY_CTRL_RIGHT ),
      entry( 164, KEY_ALT_LEFT ),
      entry( 165, KEY_ALT_RIGHT ),
      entry( 91, KEY_SUPER_LEFT ),
      entry( 92, KEY_SUPER_RIGHT )
    );

  /**
   * Whether a modifier key state is pressed or released depends on the state
   * of multiple keys (left and right). This map assigns the left and right
   * key codes to the same modifier key so that the physical state can be
   * represented by a single on-screen button (the logical state).
   * <p>
   * The 65511, 65512 are shifted alt key codes (a.k.a. the meta key).
   * </p>
   */
  private final Map<Integer, HandedSwitch> MODIFIERS_LINUX =
    Map.ofEntries(
      entry( 65505, KEY_SHIFT_LEFT ),
      entry( 65506, KEY_SHIFT_RIGHT ),
      entry( 65507, KEY_CTRL_LEFT ),
      entry( 65508, KEY_CTRL_RIGHT ),
      entry( 65511, KEY_ALT_LEFT ),
      entry( 65512, KEY_ALT_RIGHT ),
      entry( 65513, KEY_ALT_LEFT ),
      entry( 65514, KEY_ALT_RIGHT ),
      entry( 65515, KEY_SUPER_LEFT ),
      entry( 65516, KEY_SUPER_RIGHT )
    );

  /**
   * Most recently pressed non-modifier key value, empty signifies release.
   */
  private String mRegularHeld = "";

  /**
   * Stores the state of modifier keys. The contents of the map reflect the
   * state of each switch, so the reference can be final but not its contents.
   * An integer is used because keyboards usually have two separate keys for
   * each modifier, both can be pressed and released independently.
   */
  private final Map<HardwareSwitch, Boolean> mModifiers = new HashMap<>();

  private final Set<HandedSwitch> mHandedModifiers = new HashSet<>();

  /**
   * Creates a keyboard listener that publishes events when keys are either
   * pressed or released. The constructor initializes all modifier keys to
   * the released state because the native keyboard hook API does not offer
   * a way to query what keys are currently pressed.
   */
  public KeyboardListener() {
    for( final var key : modifierSwitches() ) {
      mModifiers.put( key, FALSE );
    }
  }

  /**
   * Regular printable keys are passed into this method.
   *
   * @param e The native key event.
   */
  @Override
  public void nativeKeyTyped( final NativeKeyEvent e ) {
    if( isRegular( e ) ) {
      String key = getDisplayText( e.getKeyChar() );

      if( IS_OS_LINUX ) {
        key = RAW_CODES.getOrDefault( e.getRawCode(), key );
      }

      dispatchRegular( mRegularHeld, key );
      dispatchRegular( key, "" );
    }
  }

  @Override
  public void nativeKeyPressed( final NativeKeyEvent e ) {
    dispatchModifiers( e, TRUE );

    if( e.isActionKey() && isRegular( e ) && IS_OS_WINDOWS ) {
      dispatchRegular( mRegularHeld, translate( e ) );
    }
  }

  @Override
  public void nativeKeyReleased( final NativeKeyEvent e ) {
    dispatchModifiers( e, FALSE );

    if( e.isActionKey() && isRegular( e ) && IS_OS_WINDOWS ) {
      dispatchRegular( translate( e ), "" );
    }
  }

  private String translate( final NativeKeyEvent e ) {
    final var keyCode = e.getKeyCode();
    final var text = NativeKeyEvent.getKeyText( keyCode );
    return TRANSLATE.getOrDefault( text, text );
  }

  /**
   * Sets the initial state of the modifiers.
   */
  public void initModifiers() {
    for( final var key : mModifiers.keySet() ) {
      final var state = mModifiers.get( key );

      // All modifiers keys are "false" by default, so firing fake transition
      // events from "true" to "false" will cause the GUI to repaint with the
      // text label affixed to each key, drawn in the released state. This
      // happens before the frame is set to visible.
      tryFire( key, !state, state );
    }
  }

  /**
   * Dispatches the modifier key that corresponds to the raw key code from
   * the given event. This is necessary to ensure that both left and right
   * modifier keys return the same {@link HardwareSwitch} value.
   *
   * @param e The event containing a raw key code to look up.
   */
  private void dispatchModifiers(
    final NativeKeyEvent e, final boolean pressed ) {
    final var rawCode = e.getRawCode();
    final Map<Integer, HandedSwitch> map;

    if( IS_OS_WINDOWS ) {
      map = MODIFIERS_WINDOWS;
    }
    else if( IS_OS_LINUX ) {
      map = MODIFIERS_LINUX;
    }
    else {
      return;
    }

    final var key = map.get( rawCode );

    if( key != null ) {
      if( pressed ) {
        mHandedModifiers.add( key );
      }
      else {
        mHandedModifiers.remove( key );
      }

      final var newHw = key.getHardwareSwitch();
      final var counts = new HashMap<HardwareSwitch, Integer>();

      mHandedModifiers.forEach(
        modifier -> {
          final var oldHw = modifier.getHardwareSwitch();
          counts.put( oldHw, counts.getOrDefault( oldHw, 0 ) + 1 );
        }
      );

      dispatchModifier( newHw, counts.get( newHw ) != null );
    }
  }

  private boolean isRegular( final NativeKeyEvent e ) {
    final var rawCode = e.getRawCode();

    return !((MODIFIERS_LINUX.containsKey( rawCode ) && IS_OS_LINUX) ||
      (MODIFIERS_WINDOWS.containsKey( rawCode ) && IS_OS_WINDOWS));
  }

  /**
   * Notifies of any modifier state changes. There's a bug whereby this
   * method is never called by the native library when both Left/Right Ctrl
   * keys are pressed followed by pressing either Shift key. Similarly,
   * holding both Left/Right Shift keys followed by pressing either Ctrl key
   * fails to call this method.
   *
   * @param key      Must be a modifier key.
   * @param newState {@link Boolean#FALSE} means released, {@link Boolean#TRUE}
   *                 means pressed.
   */
  private void dispatchModifier(
    final HardwareSwitch key, final boolean newState ) {
    final var oldState = mModifiers.get( key );

    // Only fire the event if the state has changed.
    tryFire( key, oldState, newState );
    mModifiers.put( key, newState );
  }

  /**
   * State for a regular (non-modifier) key has changed.
   *
   * @param o Previous key value.
   * @param n Current key value.
   */
  private void dispatchRegular( final String o, final String n ) {
    assert o != null;
    assert n != null;

    // Always fire the event, which permits double-key taps.
    fire( KEY_REGULAR, o, n );
    mRegularHeld = n;
  }

  private String getDisplayText( final char keyChar ) {
    return CHAR_CODES.getOrDefault( keyChar, String.valueOf( keyChar ) );
  }
}
