// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.Pigeon2;
import com.ctre.phoenix6.hardware.TalonFX;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String CANBUS_NAME = "canivore";
  private final CANcoder m_cc = new CANcoder(0, CANBUS_NAME);
  private final TalonFX m_fx = new TalonFX(0, CANBUS_NAME);
  private final Pigeon2 m_p2 = new Pigeon2(0, CANBUS_NAME);
  private int m_printCount = 0;

  private final DutyCycleOut m_dutycycle = new DutyCycleOut(0);

  private final XboxController m_joystick = new XboxController(0);

  private final StatusSignal<Double> m_ccpos = m_cc.getPosition();
  private final StatusSignal<Double> m_fxpos = m_fx.getPosition();
  private final StatusSignal<Double> m_p2yaw = m_p2.getYaw();
  private final StatusSignal<Double> m_ccvel = m_cc.getVelocity();
  private final StatusSignal<Double> m_fxvel = m_fx.getVelocity();
  private final StatusSignal<Double> m_p2yawRate = m_p2.getAngularVelocityZWorld();

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {

  }

  @Override
  public void robotPeriodic() {
    /* Perform basic latency compensation based on latency and current derivative */
    /* First refresh the signals */
    BaseStatusSignal.refreshAll(m_fxpos, m_fxvel, m_ccpos, m_ccvel, m_p2yaw, m_p2yawRate);

    /* Use the helper function to apply latency compensation to the signals */
    /* Since these are already refreshed we don't need to inline the refresh call */
    double ccCompensatedPos = BaseStatusSignal.getLatencyCompensatedValue(m_ccpos, m_ccvel);
    double fxCompensatedPos = BaseStatusSignal.getLatencyCompensatedValue(m_fxpos, m_fxvel);
    double p2CompensatedYaw = BaseStatusSignal.getLatencyCompensatedValue(m_p2yaw, m_p2yawRate);

    /* Print out both values so it shows how they perform */
    if (m_printCount++ > 10 && m_joystick.getAButton()) {
      m_printCount = 0;
      System.out.printf("CANcoder: Pos: %10.3f - Latency-Compensated: %10.3f - Difference: %6.5f%n", m_ccpos.getValue(), ccCompensatedPos, ccCompensatedPos - m_ccpos.getValue());
      System.out.printf("Talon FX: Pos: %10.3f - Latency-Compensated: %10.3f - Difference: %6.5f%n", m_fxpos.getValue(), fxCompensatedPos, fxCompensatedPos - m_fxpos.getValue());
      System.out.printf("Pigeon2 : Yaw: %10.3f - Latency-Compensated: %10.3f - Difference: %6.5f%n", m_p2yaw.getValue(), p2CompensatedYaw, p2CompensatedYaw - m_p2yaw.getValue());
      System.out.println();
    }
    m_fx.setControl(m_dutycycle.withOutput(m_joystick.getLeftY()));

    if (m_joystick.getLeftBumperPressed()) {
      /* Speed up the signals to reduce the latency */
      /* Make them 1000 Hz (1 ms) for this example */
      BaseStatusSignal.setUpdateFrequencyForAll(1000, m_fxpos, m_ccpos, m_p2yaw);
    }
    if (m_joystick.getRightBumperPressed()) {
      /* Slow down the signals to increase the latency */
      /* Make them 10 Hz (100 ms) for this example */
      BaseStatusSignal.setUpdateFrequencyForAll(10, m_fxpos, m_ccpos, m_p2yaw);
    }
  }

  @Override
  public void autonomousInit() {}

  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {}

  @Override
  public void teleopPeriodic() {}

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  @Override
  public void testInit() {}

  @Override
  public void testPeriodic() {}

  @Override
  public void simulationInit() {}

  @Override
  public void simulationPeriodic() {}
}
