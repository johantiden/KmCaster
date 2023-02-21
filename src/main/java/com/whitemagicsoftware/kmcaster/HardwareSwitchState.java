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

/**
 * Responsible for capturing the state of a hardware switch, which includes the
 * hardware switch's value (if any).
 */
public final class HardwareSwitchState {
  private final HardwareSwitch mHardwareSwitch;
  private final HardwareState mHardwareState;
  private final String mValue;

  /**
   * Calls {@link #HardwareSwitchState(HardwareSwitch, HardwareState, String)}
   * using an empty string for the value.
   */
  public HardwareSwitchState(
          final HardwareSwitch hardwareSwitch,
          final HardwareState hardwareState,
          final Settings userSettings ) {
    this( hardwareSwitch, hardwareState, "", userSettings );
  }

  /**
   * Constructs a new instance that represents whether a key or mouse button
   * was pressed.
   *
   * @param hwSwitch A {@link HardwareSwitch} that represents the type of
   *                 switch having the given status.
   * @param hwState  Defines whether the switch is pressed or released.
   * @param value    The value associated with the switch in the given
   *                 state. For example, this could be human-readable
   *                 text representing a pressed key code.
   */
  public HardwareSwitchState(
      final HardwareSwitch hwSwitch,
      final HardwareState hwState,
      final String value,
      final Settings userSettings ) {
    assert hwSwitch != null;
    assert hwState != null;
    assert value != null;

    mHardwareSwitch = hwSwitch;
    mHardwareState = hwState;
    mValue = hwSwitch.isModifier() ? hwSwitch.toTitleCase( userSettings ) : value;
  }

  /**
   * Answers whether this is a modifier key.
   *
   * @return {@code true} when this is a modifier key.
   */
  public boolean isModifier() {
    return mHardwareSwitch.isModifier();
  }

  /**
   * Answers whether this is a mouse-related state change.
   *
   * @return {@code true} when this is a mouse state change.
   */
  public boolean isMouse() {
    return mHardwareSwitch.isMouse();
  }

  /**
   * Returns the physical switch containing its name.
   *
   * @return The {@link HardwareSwitch} having a switch-dependent state.
   */
  public HardwareSwitch getHardwareSwitch() {
    return mHardwareSwitch;
  }

  public HardwareState getHardwareState() {
    return mHardwareState;
  }

  public String getValue() {
    return mValue;
  }

  @Override
  public boolean equals( final Object o ) {
    if( this == o ) {
      return true;
    }
    if( o == null || getClass() != o.getClass() ) {
      return false;
    }

    final HardwareSwitchState that = (HardwareSwitchState) o;

    if( mHardwareSwitch != that.mHardwareSwitch ) {
      return false;
    }
    return mHardwareState == that.mHardwareState;
  }

  @Override
  public int hashCode() {
    return 31 * mHardwareSwitch.hashCode() + mHardwareState.hashCode();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "mHardwareSwitch=" + mHardwareSwitch +
        ", mHardwareState=" + mHardwareState +
        ", mValue='" + mValue + '\'' +
        '}';
  }
}
