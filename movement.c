/*
 * movement.c
 *
 *  Created on: Feb 7, 2019
 *      Author: ryanjl9, Anthony Rosenhamer, Austin Lines
 */
#include <movement.h>
#include <lcd.h>

#define NUMINTERRUPTS 8
#define TAPEVALUE 2700

/**
 * sensor: Variable used for storing the open interface struct
 */
oi_t* sensor;

void movement_init(oi_t* s){
    sensor = s;
}

/**
 * move_forward: This method moves the CyBot forward
 */
void move_forward(int centimeters) {
    oi_setWheels(75, 75);                           //Set wheel speed
    int bot_distance = 0;


    while (checkInterrupts() == 0 && bot_distance < (centimeters * 10.65 - 2.50)) {                   //As long as the total distance traveled is less than the
        oi_update(sensor);                          //desired, keep going
        bot_distance += sensor -> distance;
    }
    botX += (bot_distance * 0.938 + 2.38 ) * cos(botAngle * 3.1416 / 180);
    botY += (bot_distance * 0.938 + 2.38 ) * sin(botAngle * 3.1416 / 180);


    oi_setWheels(0, 0);
}

/**
 * move_backward: This method moves the CyBot backwards
 */
void move_backward(int centimeters) {
    oi_setWheels(-75, -75);
    int bot_distance = 0;

    while (checkInterrupts() == 0 && bot_distance > (centimeters * -10.65 + 2.50)) {
        oi_update(sensor);
        bot_distance += sensor -> distance;
    }
    botX += (bot_distance * 0.938 + 2.38 ) * cos(botAngle * 3.1416 / 180);
    botY += (bot_distance * 0.938 + 2.38 ) * sin(botAngle * 3.1416 / 180);

    oi_setWheels(0, 0);
}

/**
 * turn_right: This method turns the CyBot right
 */
void turn_right(int degrees) {
    oi_setWheels(-75, 75);
    int bot_rotation = 0;

    while (checkInterrupts() == 0 && bot_rotation > degrees * -0.93) {
        oi_update(sensor);
        bot_rotation += sensor -> angle;
    }
    botAngle += bot_rotation / 0.93;

    oi_setWheels(0, 0);
}

/**
 * turn_left: This method turns the CyBot left
 */
void turn_left(int degrees) {
    oi_setWheels(75, -75);                      //Sets wheel speeds to opposites so the roomba spins left
    int bot_rotation = 0;

    while (checkInterrupts() == 0 && bot_rotation < degrees * 0.93) {
        oi_update(sensor);
        bot_rotation += sensor -> angle;
    }
    botAngle += bot_rotation / 0.93;

    oi_setWheels(0, 0);
}

int checkInterrupts(){
        if (sensor -> wheelDropLeft || sensor -> cliffFrontLeft || sensor -> cliffLeft || sensor -> bumpLeft) {
            currentPosition.appCommand = 'l';
            return 1;
        }
        if (sensor -> wheelDropRight || sensor -> cliffFrontRight || sensor -> cliffRight || sensor -> bumpRight) {
            currentPosition.appCommand = 'r';
            return 2;
        }
//        else if (sensor -> cliffLeftSignal > TAPEVALUE) {
//            currentPosition.appCommand = '1';
//            return 3;
//        }
//        else if (sensor -> cliffFrontLeftSignal > TAPEVALUE) {
//            currentPosition.appCommand = '2';
//            return 4;
//        }
//        else if (sensor -> cliffFrontRightSignal > TAPEVALUE) {
//            currentPosition.appCommand = '3';
//            return 5;
//        }
//        else if (sensor -> cliffRightSignal > TAPEVALUE) {
//            currentPosition.appCommand = '4';
//            return 6;
//        }
    currentPosition.appCommand = 'b';
	return 0;
}

































