package com.whitemagicsoftware.kmcaster;

import com.whitemagicsoftware.kmcaster.ui.AutofitLabel;

import javax.swing.*;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.whitemagicsoftware.kmcaster.HardwareSwitch.*;
import static java.util.Optional.*;
import static javax.swing.SwingConstants.*;

/**
 * Used for initializing the {@link AutofitLabel} instances. Labels are
 * centre-aligned vertically and horizontally by default. See also
 * the hardware switch insets from {@link HardwareImages}, which defines
 * the safe drawing area for each label.
 */
public enum LabelConfig {
  LABEL_SHIFT( KEY_SHIFT ),
  LABEL_SUPER( KEY_SUPER ),
  LABEL_CTRL( KEY_CTRL ),
  LABEL_ALT( KEY_ALT ),
  LABEL_REGULAR( KEY_REGULAR ),
  LABEL_REGULAR_NUM_MAIN(),
  LABEL_REGULAR_NUM_SUPERSCRIPT( TOP, LEFT ),
  LABEL_REGULAR_COUNTER( TOP, RIGHT ),
  LABEL_MOUSE_EXTRA( MOUSE_EXTRA );

  /**
   * A value of {@code null} indicates multiple labels adorn the switch.
   */
  private final HardwareSwitch mHardwareSwitch;
  private final int mHorizontalAlign;
  private final int mVerticalAlign;

  /**
   * Centres the label vertically and horizontally.
   */
  LabelConfig() {
    this( null );
  }

  /**
   * Centres the label vertically and horizontally.
   *
   * @param hwSwitch The switch to associate with the single label.
   */
  LabelConfig( final HardwareSwitch hwSwitch ) {
    this( hwSwitch, CENTER, CENTER );
  }

  /**
   * Creates a configuration for a multi-label hardware switch (such as
   * a regular key).
   *
   * @param vAlign Vertical alignment.
   * @param hAlign Horizontal alignment.
   */
  @SuppressWarnings("SameParameterValue")
  LabelConfig( final int vAlign, final int hAlign ) {
    this( null, vAlign, hAlign );
  }

  /**
   * Creates a configuration for a hardware switch that supports a single
   * label (such as a modifier key).
   *
   * @param hwSwitch The switch to associate with the single label.
   * @param vAlign   Vertical alignment.
   * @param hAlign   Horizontal alignment.
   */
  LabelConfig(
      final HardwareSwitch hwSwitch, final int vAlign, final int hAlign ) {
    mHardwareSwitch = hwSwitch;
    mVerticalAlign = vAlign;
    mHorizontalAlign = hAlign;
  }

  static LabelConfig valueFrom( final HardwareSwitch hwSwitch, final Settings userSettings ) {
    for( final var lc : values() ) {
      if( lc.isHardwareSwitch( hwSwitch ) ) {
        return lc;
      }
    }

    throw new NoSuchElementException( hwSwitch.toTitleCase( userSettings ) );
  }

  /**
   * If present, a return value indicates the switch has a single label and
   * is associated with it.
   *
   * @return A switch associated with a label, or empty if multi-labelled.
   */
  Optional<HardwareSwitch> getHardwareSwitch() {
    return ofNullable( mHardwareSwitch );
  }

  /**
   * Returns an alignment value from {@link SwingConstants}.
   *
   * @return An indicator of left, right, or centered alignment.
   */
  int getHorizontalAlign() {
    return mHorizontalAlign;
  }

  /**
   * Returns an alignment value from {@link SwingConstants}.
   *
   * @return An indicator of top, bottom, or centered alignment.
   */
  int getVerticalAlign() {
    return mVerticalAlign;
  }

  /**
   * Returns a blank space when no {@link HardwareSwitch} is assigned.
   *
   * @return The title case version of the hardware switch, or a space if
   * there is no direct correlation.
   */
  String toTitleCase( final Settings userSettings ) {
    return mHardwareSwitch == null ? " " : mHardwareSwitch.toTitleCase( userSettings );
  }

  /**
   * Returns the number of values in the enumeration.
   *
   * @return {@link #values()}.length.
   */
  static int size() {
    return values().length;
  }

  private boolean isHardwareSwitch( final HardwareSwitch hwSwitch ) {
    final var os = getHardwareSwitch();
    return os.isPresent() && os.get() == hwSwitch;
  }
}
