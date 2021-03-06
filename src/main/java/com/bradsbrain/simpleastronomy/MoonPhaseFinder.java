/*
 *  Copyright 2011 Brad Parks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bradsbrain.simpleastronomy;

import java.util.Calendar;
import java.util.Date;

public class MoonPhaseFinder {

	private static final int _31_DAYS_AS_MINUTES = 31 * 24 * 60;
	
    private static final MoonFinder newMoonFinder = new NewMoonFinder();
    
    private static final MoonFinder fullMoonFinder = new FullMoonFinder();

    public enum MoonPhase {
        NEW,
        WAXINGCRESCENT,
        FIRSTQUARTER,
        WAXINGGIBBOUS,
        FULL,
        WANINGGIBBOUS,
        LASTQUARTER,
        WANINGCRESCENT;


        static MoonPhase finder(double percent) {
            return FULL;
        }
    }

    /**
     * Someday this will return a descriptive MoonPhase enum when handed a cal/date
     *
     * @param cal
     * @return
     */
    public static MoonPhase findMoonPhaseAt(Calendar cal) {
        return null;
    }

    public static Date findFullMoonFollowing(Calendar cal) {
        return findDatePassingBounds(cal, fullMoonFinder);
    }

    public static Date findNewMoonFollowing(Calendar cal) {
        return findDatePassingBounds(cal, newMoonFinder);
    }

    public static Date findFullMoonFollowing(Date date) {
        long time = date.getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return findDatePassingBounds(cal, fullMoonFinder);
    }

    public static Date findNewMoonFollowing(Date date) {
        long time = date.getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return findDatePassingBounds(cal, newMoonFinder);
    }

    /**
     * Finds a type of moon after the given calendar.  Uses a binary search.  Recursive.
     *
     * @param cal         the calendar date for which to compute the moon position
     * @param moonChecker the NewMoon or FullMoon checker
     * @return the forward date which passes the given bounds provided
     */
    private static Date findDatePassingBounds(Calendar cal, MoonFinder moonFinder) {
    	int startMinutes =  0, endMinutes = _31_DAYS_AS_MINUTES;
    	while (1 < (endMinutes - startMinutes)) {
    		int middleMinutes = startMinutes + ((endMinutes - startMinutes) / 2);
    		Calendar middleCal = BaseUtils.getSafeLocalCopy(cal.getTimeInMillis());
    		middleCal.add(Calendar.MINUTE, middleMinutes);
    		
    		double percent = 100 * MoonPhaseFinder.getMoonVisiblePercent(middleCal);
    		double angle = MoonPhaseFinder.getMoonAngle(middleCal);
    		
    		if (moonFinder.isMoonBefore(angle, percent)) {
    			endMinutes = middleMinutes;
    		} else {
    			startMinutes = middleMinutes;
    		}
    	}
    	
    	Calendar myCal = BaseUtils.getSafeLocalCopy(cal.getTimeInMillis());
    	myCal.add(Calendar.MINUTE, endMinutes);
    	return myCal.getTime();
    }

    /**
     * Returns a (much-too-)high-precision value for the amount of moon visible.
     * Value will be somewhere in the range 0% to 100%  (i.e. 0.00 to 1.00)
     *
     * @param cal
     * @return percent of moon which is visible
     */
    public static double getMoonVisiblePercent(Calendar cal) {
        double moonAngle = getMoonAngle(cal);
        return BaseUtils.useLessPrecision(0.5 * (1 - BaseUtils.cosDegrees(moonAngle)), 3);
    }

    /**
     * The moon angle.  For that we need the sun's position and the moon's position. </br>
     * The moon angle will be in the range 0 to 360.  <br/>
     * 0 or 360 is NEW, 180 is FULL
     *
     * @param cal
     * @return the angle of the moon in relation to the earth
     */
    public static double getMoonAngle(Calendar cal) {
        Calendar myCal = BaseUtils.getSafeLocalCopy(cal.getTimeInMillis());
        SunPosition sunPos = new SunPosition(myCal);
        MoonPosition moonPos = new MoonPosition(myCal);

        double angleAge = moonPos.getTrueLongitude() - sunPos.getEclipticLongitude();
        if (angleAge < 0) {
            return 360 + angleAge;
        }
        return angleAge;
    }
    
}
