import wpilib.simulation as sim
from wpilib import RobotController, DriverStation

from wpimath.system.plant import DCMotor
from wpimath.units import radiansToRotations

from pyfrc.physics.core import PhysicsInterface
from phoenix6 import unmanaged

import typing

if typing.TYPE_CHECKING:
    from robot import MyRobot


class PhysicsEngine:

    def __init__(self, physics_controller: PhysicsInterface, robot: "MyRobot"):
        self.physics_controller = physics_controller

        # Create a DCMotorSim for physics sim
        self.motor_sim = sim.DCMotorSim(DCMotor.krakenX60FOC(1), 1, 0.01)
        # Keep a reference to the cancoder sim state so we can update it
        self.cancoder_sim = robot.cancoder.sim_state

        # Keep a reference to the controller so we can drive a simulated motor
        self.controller = robot.controller

    def update_sim(self, now: float, tm_diff: float) -> None:
        """
        Called when the simulation parameters for the program need to be
        updated.

        :param now: The current time as a float
        :param tm_diff: The amount of time that has passed since the last
                        time that this function was called
        """
        # If the driver station is enabled, then feed enable for phoenix devices
        if DriverStation.isEnabled():
            unmanaged.feed_enable(100)

        self.cancoder_sim.set_supply_voltage(RobotController.getBatteryVoltage())
        self.motor_sim.setInputVoltage(self.controller.getLeftY() * 12)
        self.motor_sim.update(tm_diff)
        self.cancoder_sim.set_raw_position(radiansToRotations(self.motor_sim.getAngularPosition()))
        self.cancoder_sim.set_velocity(radiansToRotations(self.motor_sim.getAngularVelocity()))
