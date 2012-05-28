/**
 * 
 */
package Bot;

import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.RobotDeathEvent;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;

/**
 * @author Bebert
 * 
 */
public class BebertBot extends Robot {

	private String target = null;
	private int count;
	private double gunTurnAmt;

	public double randomDegree(double min, double max) {

		double random = (double) (Math.random() * (max - min)) + min;

		return random;

	}

	public void run() {

		while (true) {
			count++;
			if (target == null) {
				setAdjustGunForRobotTurn(false);
				setAdjustRadarForGunTurn(false);
				setAdjustRadarForRobotTurn(false);
				ahead(randomDegree(0, 100));
				turnRight(randomDegree(0, 60));
				back(randomDegree(0, 100));
			} else {
//				ahead(randomDegree(0, 200));
			}
			
			if (count > 11) {
				setAdjustGunForRobotTurn(false);
				target = null;
			}
		}

	}

	public void onScannedRobot(ScannedRobotEvent e) {
		

		double EnemyDist = e.getDistance();
		double EnemySpeed = e.getVelocity();
		double EnemyBearing = e.getBearing();
		double EnemyHead = e.getHeading();
		
		double absoluteBearing = getHeading() + e.getBearing();
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing
				- getGunHeading());

		// If we have a target, and this isn't it, return immediately
		// so we can get more ScannedRobotEvents.
		if (target != null && !e.getName().equals(target)) {
			return;
		}

		// If we don't have a target, well, now we do!
		if (target == null) {
			target = e.getName();
			out.println("Tracking " + target);
		}

		// This is our target. Reset count (see the run method)
		count = 0;
		// If our target is too far away, turn and move toward it.
		if (e.getDistance() > 150) {
			
			setAdjustGunForRobotTurn(true);
			turnGunRight(bearingFromGun); // Try changing these to setTurnGunRight,
			
			ahead(randomDegree(0, 100));
			turnRight(randomDegree(0, 10));
			return;
		}

		if (Math.abs(bearingFromGun) <= 3) {
			turnGunRight(bearingFromGun);
			if (getGunHeat() == 0) {
				fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
			}
		} else {
			turnGunRight(bearingFromGun);
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

	}

	@Override
	public void onHitRobot(HitRobotEvent event) {

	}

	@Override
	public void onHitWall(HitWallEvent event) {

	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {

	}

	@Override
	public void onRoundEnded(RoundEndedEvent event) {

	}

	@Override
	public void onStatus(StatusEvent e) {

	}

}
