/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

//Contributer: Mark Carman

package org.usfirst.frc.team888.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Encoder;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends IterativeRobot {
	private static final String kDefaultAuto = "Default";
	private static final String kCustomAuto = "My Auto";
	private String m_autoSelected;
	private SendableChooser<String> m_chooser = new SendableChooser<>();

	Victor rearLeft = new Victor(7);
	Victor rearRight = new Victor(6);
	Victor frontLeft = new Victor(9);
	Victor frontRight = new Victor(8);
	
	Joystick leftStick = new Joystick(0); // set to ID 1 in DriverStation
	Joystick rightStick = new Joystick(1); // set to ID 2 in DriverStation
	
	Encoder leftEncoder = new Encoder(0, 1, true, Encoder.EncodingType.k4X);

	Encoder rightEncoder = new Encoder(2, 3, false, Encoder.EncodingType.k4X);

	
	
	//DigitalInput triggerSensor = new DigitalInput(1); 
	
	
	/*Victor leftShooterWheel = new Victor(3);
	Victor shooterAngle = new Victor(4);
	
	Victor rightShooterWheel = new Victor(5);

	Victor lights = new Victor(2);

	Joystick shooterStick = new Joystick(2);

	Compressor mainCompressor = new Compressor(1);
	DoubleSolenoid piston = new DoubleSolenoid(0, 1);

	Timer timer = new Timer();*/
	//Timer shootTimer = new Timer();
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		m_chooser.addDefault("Default Auto", kDefaultAuto);
		m_chooser.addObject("My Auto", kCustomAuto);
		SmartDashboard.putData("Auto choices", m_chooser);
		
		
		
		leftEncoder.setMaxPeriod(.1);
		leftEncoder.setMinRate(10);
		leftEncoder.setReverseDirection(false);
		leftEncoder.setSamplesToAverage(7);
		leftEncoder.setDistancePerPulse(0.0784313725490196);
		
		rightEncoder.setMaxPeriod(.1);
		rightEncoder.setMinRate(10);
		rightEncoder.setReverseDirection(true);
		rightEncoder.setSamplesToAverage(7);
		rightEncoder.setDistancePerPulse(0.0784313725490196);
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		leftEncoder.reset();
		rightEncoder.reset();
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		if(leftEncoder.getDistance() < 24) {
			//left side forward is positive
			rearLeft.set(0.5);
			frontLeft.set(0.5);
		}
		else {
			rearLeft.set(0);
			frontLeft.set(0);
		}
		if(rightEncoder.getDistance() < 24) {
			//right side forward is negative
			rearRight.set(-0.5);
			frontRight.set(-0.5);
		}
		else {
			rearRight.set(0);
			frontRight.set(0);
		}
	}

	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		
		//Drive Section
		//The left side is negated because it needs to be that way to get 
		//the motors to spin in the forward direction when the joystick is pushed forward
		//Left side forward is positive
		double leftStickValue = -leftStick.getRawAxis(1);
		//right side forward is negative
		double rightStickValue = rightStick.getRawAxis(1);
		
		
		if (!leftStick.getRawButton(1) && !rightStick.getRawButton(1)) {
			leftStickValue *= 0.7;
			rightStickValue *= 0.7;
		}

		rearLeft.set(leftStickValue);
		frontLeft.set(leftStickValue);
		rearRight.set(rightStickValue);
		frontRight.set(rightStickValue);
		
		System.out.println("Distance:");
		System.out.println(leftEncoder.getDistance());
		System.out.println(rightEncoder.getDistance());
		
		
		 //Auto Firing section
		/*boolean sensorTripped = triggerSensor.get();
		 * 
		System.out.println("potatoes:");
		System.out.println(sensorTripped);
		System.out.println(triggerSensor.get());*/
		
		/*double gamepadRightJoystick = shooterStick.getRawAxis(5);
		double gamepadLeftTrigger = shooterStick.getRawAxis(2);
		double gamepadRightTrigger = shooterStick.getRawAxis(3);
		double shooterSpeed = (1 - rightStick.getRawAxis(2)) + 0.1;
		double timeSinceStart = timer.get();
		
		
		shooterAngle.set(gamepadRightJoystick*0.5);
		
		leftShooterWheel.set(gamepadLeftTrigger);
		rightShooterWheel.set(-gamepadRightTrigger);

		

		if ((gamepadLeftTrigger > 0.8) && (gamepadRightTrigger > 0.8)) {//(sensorTripped) {
			lights.set(0);

			if (piston.get() == Value.kForward) {
				lights.set(1);
			} else {
				lights.set(0);
			}


			if (triggerSensor.get()) {
				if (piston.get() == Value.kReverse) {
					piston.set(DoubleSolenoid.Value.kForward);
				} else {
					piston.set(DoubleSolenoid.Value.kReverse);
				}
			} else {
				piston.set(DoubleSolenoid.Value.kReverse);
			}
		} else {
			piston.set(DoubleSolenoid.Value.kReverse);
			lights.set(Math.abs(Math.sin(timeSinceStart/30*(180/Math.PI))));
		}
		*/
		
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}
