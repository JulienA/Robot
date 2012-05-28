/**
 * 
 */
package Bot;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.Condition;
import robocode.CustomEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;

/**
 * @author Bebert
 * 
 */
public class BebertBot extends AdvancedRobot {

	Target target; // our current enemy
	final double PI = Math.PI; // just a constant
	int direction = 1; // direction we are heading...1 = forward, -1 = backwards

	double firePower; // the power of the shot we will be using - set by do
						// firePower()

	public double randomDegree(double min, double max) {

		double random = (double) (Math.random() * (max - min)) + min;

		return random;

	}

	public void run() {

		addCustomEvent(new Condition("bulletfired") {
			public boolean test() {
				double delta = target.oldEnergy - target.newEnergy; // delta is
																	// the value
																	// by
				// which the energy of
				// the enemy just
				// changed
				target.oldEnergy = target.newEnergy; // we should do this now,
														// so that the one
				// bullet-firing does not generate
				// multiple events
				return delta >= 0.1 && delta <= 3; // if it changed by this
													// much, chances are a
													// bullet is in the air
			};
		});

		target = new Target();
		target.distance = 100000; // initialise the distance so that we can
									// select a target

		// the next two lines mean that the turns of the robot, gun and radar
		// are independant
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		turnRadarRightRadians(2 * PI); // turns the radar right around to get a
										// view of the field
		while (true) {
			doMovement(); // Move the bot

			doFirePower(); // select the fire power to use
			doScanner(); // Oscillate the scanner over the bot
			doGun();
			out.println(target.distance); // move the gun to predict where the
											// enemy will be
			fire(firePower);
			execute(); // execute all commands
		}

	}

	private void doMovement() {
		if (getTime() % 20 == 0) { // every twenty 'ticks'
			direction *= -1; // reverse direction
			setAhead(direction * 300); // move in that direction

		}

		setTurnRight(randomDegree(-180, 180));

		// setTurnRightRadians(target.bearing + (PI / 2)); // every turn move to
		// circle strafe the
		// enemy
	}

	private void doFirePower() {
		firePower = 400 / target.distance;// selects a bullet power based on our
											// distance away from the target

	}

	private void doScanner() {
		double radarOffset;
		if (getTime() - target.ctime > 4) { // if we haven't seen anybody for a
											// bit....

			radarOffset = 360; // rotate the radar to find a target
		} else {

			// next is the amount we need to rotate the radar by to scan where
			// the target is now
			radarOffset = getRadarHeadingRadians()
					- absbearing(getX(), getY(), target.x, target.y);

			// this adds or subtracts small amounts from the bearing for the
			// radar to produce the wobbling
			// and make sure we don't lose the target

			if (radarOffset < 0)
				radarOffset -= PI / 8;
			else
				radarOffset += PI / 8;
		}
		// turn the radar
		setTurnRadarLeftRadians(NormaliseBearing(radarOffset));
	}

	private void doGun() {

		// works out how long it would take a bullet to travel to where the
		// enemy is *now*

		// this is the best estimation we have
		long time = getTime()
				+ (int) (target.distance / (20 - (3 * firePower)));

		// offsets the gun by the angle to the next shot based on linear
		// targeting provided by the enemy class
		double gunOffset = getGunHeadingRadians()
				- absbearing(getX(), getY(), target.guessX(time),
						target.guessY(time));
		setTurnGunLeftRadians(NormaliseBearing(gunOffset));
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		// if we have found a closer robot....
		if ((e.getDistance() < target.distance) || (target.name == e.getName())) {
			// the next line gets the absolute bearing to the point where the
			// bot is
			double absbearing_rad = (getHeadingRadians() + e
					.getBearingRadians()) % (2 * PI);
			// this section sets all the information about our target

			target.name = e.getName();

			target.newEnergy = e.getEnergy();
			// works out the x coordinate of where the target is
			target.x = getX() + Math.sin(absbearing_rad) * e.getDistance();
			// works out the y coordinate of where the target is
			target.y = getY() + Math.cos(absbearing_rad) * e.getDistance();
			target.bearing = e.getBearingRadians();
			target.head = e.getHeadingRadians();
			target.ctime = getTime(); // game time at which this scan was
										// produced
			target.speed = e.getVelocity();
			target.distance = e.getDistance();
		}
	}

	@Override
	public void onBulletHit(BulletHitEvent event) {

	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {

	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {

	}

	@Override
	public void onDeath(DeathEvent event) {

	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		direction *= -1;
		setTurnRight(randomDegree(-180, 180));
		setBack(Math.random());
	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
		direction *= -1;
		setTurnRight(randomDegree(-180, 180));
		setBack(Math.random());
	}

	@Override
	public void onHitWall(HitWallEvent event) {
		direction *= -1;
		setTurnRight(randomDegree(-180, 180));
		setBack(Math.random());
	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		if (event.getName() == target.name)
			target.distance = 10000;
	}

	@Override
	public void onRoundEnded(RoundEndedEvent event) {

	}

	@Override
	public void onStatus(StatusEvent e) {

	}

	public void onCustomEvent(CustomEvent e) {
		// If our custom event "bulletfired" went off,
		if (e.getCondition().getName().equals("bulletfired")) {
			direction *= -1;
			setTurnRight(randomDegree(-180, 180));
			setBack(Math.random());
		}
	}

	// if a bearing is not within the -pi to pi range, alters it to provide the
	// shortest angle
	double NormaliseBearing(double ang) {
		if (ang > PI)
			ang -= 2 * PI;
		if (ang < -PI)
			ang += 2 * PI;
		return ang;
	}

	// if a heading is not within the 0 to 2pi range, alters it to provide the
	// shortest angle
	double NormaliseHeading(double ang) {
		if (ang > 2 * PI)
			ang -= 2 * PI;
		if (ang < 0)
			ang += 2 * PI;
		return ang;
	}

	// returns the distance between two x,y coordinates
	public double getrange(double x1, double y1, double x2, double y2) {
		double xo = x2 - x1;
		double yo = y2 - y1;
		double h = Math.sqrt(xo * xo + yo * yo);
		return h;
	}

	// gets the absolute bearing between to x,y coordinates
	public double absbearing(double x1, double y1, double x2, double y2) {
		double xo = x2 - x1;
		double yo = y2 - y1;
		double h = getrange(x1, y1, x2, y2);
		if (xo > 0 && yo > 0) {
			return Math.asin(xo / h);
		}
		if (xo > 0 && yo < 0) {
			return Math.PI - Math.asin(xo / h);
		}
		if (xo < 0 && yo < 0) {
			return Math.PI + Math.asin(-xo / h);
		}
		if (xo < 0 && yo > 0) {
			return 2.0 * Math.PI - Math.asin(-xo / h);
		}
		return 0;
	}

}
