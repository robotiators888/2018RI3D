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
import edu.wpi.first.wpilibj.Relay;

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

	private Victor rearLeft = new Victor(7); //test bot is 7, real 0
	private Victor rearRight = new Victor(6); //test bot is 6, real 2
	private Victor frontLeft = new Victor(9); //test bot is 9, real 1
	private Victor frontRight = new Victor(8); //test bot is 8, real 3
	
	private Joystick gamepad = new Joystick(2);
	
	private Encoder leftEncoder = new Encoder(2, 3, true, Encoder.EncodingType.k4X);

	private Encoder rightEncoder = new Encoder(0, 1, false, Encoder.EncodingType.k4X);

	private boolean drivingForwardAuto1 = false;
	private boolean turningClockwiseAuto1 = false;
	
	private Relay spinnyLight = new Relay(0, Relay.Direction.kForward);
	
	private Compressor mainCompressor = new Compressor(1);
	private DoubleSolenoid boxGrabber = new DoubleSolenoid(0, 1);
	private DoubleSolenoid boxHolder = new DoubleSolenoid(2,3);
	private DoubleSolenoid boxPusher = new DoubleSolenoid(4,5);
	
	private Victor rightIntake = new Victor(4);
	private Victor leftIntake = new Victor(5);
	
	private boolean armsSearching = false;
	private long armsSearchingTime = 0;
	private boolean boxGrabberClosing = false;
	private long boxGrabberClosingTime = 0;
	
	private DigitalInput boxGrabberSensor = new DigitalInput(4);
	
	private Victor boxLifter1 = new Victor(6);
	private Victor boxLifter2 = new Victor(7);
	
	//DigitalInput triggerSensor = new DigitalInput(1); 
	
	
	/*Victor leftShooterWheel = new Victor(3);
	Victor shooterAngle = new Victor(4);
	
	Victor rightShooterWheel = new Victor(5);

	Victor lights = new Victor(2);

	

	DoubleSolenoid piston = new DoubleSolenoid(0, 1);

	Timer timer = new Timer();*/
	//Timer shootTimer = new Timer();
	
	private static final double DEGREES_PER_INCH = 3.943044906900329;
	private static final double INCHES_PER_COUNT = 0.0784313725490196;
	
	//Key Mappings
	private static final int BOX_INTAKE_BUTTON = 3;
	private static final int BOX_INTAKE_FORCE_CLOSE_ARMS_BUTTON = 6;
	private static final int LEFT_DRIVE_AXIS = 1;
	private static final int RIGHT_DRIVE_AXIS = 5;
	private static final int LIFTER_DOWN_AXIS = 6;
	private static final int LIFTER_UP_AXIS = 7;
	
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
		leftEncoder.setDistancePerPulse(INCHES_PER_COUNT);
		
		rightEncoder.setMaxPeriod(.1);
		rightEncoder.setMinRate(10);
		rightEncoder.setReverseDirection(true);
		rightEncoder.setSamplesToAverage(7);
		rightEncoder.setDistancePerPulse(INCHES_PER_COUNT);
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
		resetEncoders();
		drivingForwardAuto1 = true;
		turningClockwiseAuto1 = true;
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		if(drivingForwardAuto1) {
			drivingForwardAuto1 = driveForwardDistance(24, 0.7);
			if(!drivingForwardAuto1) {
				resetEncoders();
			}
		}
		else if(turningClockwiseAuto1) {
			turningClockwiseAuto1 = turnDegreesOnDime(-90, 0.8);
			if(!turningClockwiseAuto1) {
				resetEncoders();
			}
		}
		else {
			rearLeft.set(0);
			frontLeft.set(0);
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
		double leftStickValue = -gamepad.getRawAxis(LEFT_DRIVE_AXIS);
		//right side forward is negative
		double rightStickValue = gamepad.getRawAxis(RIGHT_DRIVE_AXIS);
		
		/*
		if (!leftStick.getRawButton(1) && !rightStick.getRawButton(1)) {
			leftStickValue *= 0.7;
			rightStickValue *= 0.7;
		}*/

		rearLeft.set(leftStickValue);
		frontLeft.set(leftStickValue);
		rearRight.set(rightStickValue);
		frontRight.set(rightStickValue);
		
		System.out.println("Distance:");
		System.out.println(leftEncoder.getDistance());
		System.out.println(rightEncoder.getDistance());
		
		spinnyLight.set(Relay.Value.kForward);
		
		
		//Arm Controlling Code
		if(gamepad.getRawButton(BOX_INTAKE_BUTTON)) {
			//open arms
			boxGrabber.set(DoubleSolenoid.Value.kForward);
			//turn on rollers
			leftIntake.set(-0.5);
			rightIntake.set(0.5);
			//set state to searching
			armsSearching = true;
			boxGrabberClosing = false;
			armsSearchingTime = System.currentTimeMillis();
		}
		
		if((armsSearching && (System.currentTimeMillis()-armsSearchingTime > 500)) || gamepad.getRawButton(BOX_INTAKE_FORCE_CLOSE_ARMS_BUTTON)) {
			if(boxGrabberSensor.get() || gamepad.getRawButton(BOX_INTAKE_FORCE_CLOSE_ARMS_BUTTON)) {
				//close arms
				boxGrabber.set(DoubleSolenoid.Value.kReverse);
				//leave rollers on
				//set state to intaking box
				armsSearching = false;
				boxGrabberClosing = true;
				boxGrabberClosingTime = System.currentTimeMillis();
			}
		}
		
		if(boxGrabberClosing && (System.currentTimeMillis()-boxGrabberClosingTime) > 1000) {
			//turn off rollers
			leftIntake.set(0);
			rightIntake.set(0);
			boxGrabberClosing = false;
		}
		
		//Lifter Controlling Code
		//the two motors spin opposite directions
		boxLifter1.set(gamepad.getRawAxis(LIFTER_UP_AXIS)-gamepad.getRawAxis(LIFTER_DOWN_AXIS));
		boxLifter2.set(-(gamepad.getRawAxis(LIFTER_UP_AXIS)-gamepad.getRawAxis(LIFTER_DOWN_AXIS)));
		
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
	
	/**
	 * This function resets the drive encoders.
	 * Call this function before every encoder based function
	 */
	private void resetEncoders() {
		leftEncoder.reset();
		rightEncoder.reset();
	}
	
	/**
	 * This function drives forward a specified distance at a specified speed.
	 * It can be used to drive backwards by using a negative distance with a negative speed.
	 * 
	 * Make sure to call resetEncoders() before running this function.
	 * 
	 * WARNING: WILL ERROR IF SPEED IS NOT BETWEEN 0 AND 1
	 * @param distanceToDrive
	 * @param speed
	 * @return
	 */
	private boolean driveForwardDistance(double distanceToDrive, double speed) {
		if(speed > 1 || speed < 0) {
			throw new IllegalArgumentException("The speed should be a number between 0 and 1");
		}
		//If we are driving Forward, the distance is positive. Driving forward will then be a 1.
		//If we are driving Backwards, the distance is negative. Driving forward will then be a -1.
		int drivingForward = (int) (distanceToDrive/Math.abs(distanceToDrive));
		boolean leftSideFinished = Math.abs(leftEncoder.getDistance()) >= Math.abs(distanceToDrive);
		boolean rightSideFinished = Math.abs(rightEncoder.getDistance()) >= Math.abs(distanceToDrive);
		if(!leftSideFinished) {
			//left side forward is positive
			rearLeft.set(speed*drivingForward);
			frontLeft.set(speed*drivingForward);
		}
		else {
			//we made it to this distance, turn off motors
			rearLeft.set(0);
			frontLeft.set(0);
		}
		if(!rightSideFinished) {
			//right side forward is negative
			rearRight.set(-speed*drivingForward);
			frontRight.set(-speed*drivingForward);
		}
		else {
			//we made it to this distance, turn off motors
			rearRight.set(0);
			frontRight.set(0);
		}
		//Return true if running, return false if finished
		return !(leftSideFinished && rightSideFinished);
	}
	
	private boolean turnDegreesOnDime(double angle, double speed) {
		if(speed > 1 || speed < 0) {
			throw new IllegalArgumentException("The speed should be a number between 0 and 1");
		}
		int spinClockwise = (int) (angle/Math.abs(angle));
		double leftDegreesRotated = DEGREES_PER_INCH*leftEncoder.getDistance();
		double rightDegreesRotated = DEGREES_PER_INCH*rightEncoder.getDistance();
		boolean leftSideFinished = Math.abs(leftDegreesRotated) >= Math.abs(angle);
		boolean rightSideFinished = Math.abs(rightDegreesRotated) >= Math.abs(angle);
		
		if(!leftSideFinished) {
			System.out.println("Left:");
			System.out.println(leftDegreesRotated);
			System.out.println(leftEncoder.getDistance());
			rearLeft.set(speed*spinClockwise);
			frontLeft.set(speed*spinClockwise);
		} 
		else {
			//we made it to this angle, turn off motors
			rearLeft.set(0); 
			frontLeft.set(0);
		}
		if(!rightSideFinished) {
			System.out.println("Right:");
			System.out.println(rightDegreesRotated);
			System.out.println(rightEncoder.getDistance());
			rearRight.set(speed*spinClockwise);
			frontRight.set(speed*spinClockwise);
		}
		else {
			//we made it to this angle, turn off motors
			rearRight.set(0);
			frontRight.set(0);
		}
		
		return !(leftSideFinished && rightSideFinished);
	}
}
