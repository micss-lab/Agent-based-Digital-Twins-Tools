package playground;

import java.util.logging.Logger;


import java.util.Scanner;

public class HelloWorld {

    static public void main(String[] args) {
        Logger logger = Logger.getLogger(HelloWorld.class.getName());

        System.out.print("Which doctor do you prefer?\n");
        Scanner in = new Scanner(System.in);
        String doctorName = in .next();
        String[] doctor = {
                "Joe",
                "Helen",
                "Chandler",
                "John"
        };
        String[] days = {
                "Monday",
                "Wednesday",
                "Thursday",
                "Saturday"
        };
        boolean doctorFound = false;
        boolean dayFound = false;
        for (String s: doctor) {
            if (s.equals(doctorName)) {
                System.out.print("Found the doctor!\n");
                logger.info("Doctor found: " + doctorName);
                doctorFound = true;
                break;
            }
        }
        if (doctorFound) {
            System.out.println("When do you want to schedule your appointment?");
            String dayPicked = in .next();
            for (String p: days) {
                if (p.equals(dayPicked)) {
                    System.out.println("You are booked for " + dayPicked);
                    logger.info("Appointment booked on " + dayPicked);
                    dayFound = true;
                    System.exit(0);
                }
            }
            if (dayFound == false) {
                logger.info("Sorry, we are not available on that day.");
                logger.info("Exiting application.");
                System.exit(0);
            }
        } else {
            logger.info("Invalid doctor name");
        }
        logger.info("Exiting application.");
        System.exit(0);
    }

}