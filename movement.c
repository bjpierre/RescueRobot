/*
 * movement.c
 *
 *  Created on: Feb 7, 2019
 *      Author: ryanjl9, Anthony Rosenhamer, Austin Lines
 */
#include <movement.h>
#include <lcd.h>

#define NUMINTERRUPTS 8

/**
 * sensor: Variable used for storing the open interface struct
 */
oi_t* sensor;

/**
 * direction: This array holds the current direction we want
 * the cybot to move
 *
 * {FORWARD, LEFT, BACKWARD, RIGHT}
 */
uint32_t sensor_interrupts[NUMINTERRUPTS];

int direction[] = {0, 0, 0, 0};
int stop_movement = 0;

void movement_init(oi_t* s){
    sensor = s;

//  uint32_t temp[NUMINTERRUPTS] = {
//      s -> wheelDropLeft,
//      s -> wheelDropRight,
//      s -> bumpLeft,
//      s -> bumpRight,
//      s -> cliffLeft,
//      s -> cliffFrontLeft,
//      s -> cliffFrontRight,
//      s -> cliffRight
//  };

//  memcpy(sensor_interrupts, temp, NUMINTERRUPTS);

}

/**
 * move_forward: This method moves the CyBot forward
 */
void move_forward(int centimeters) {
    oi_setWheels(75, 75);                           //Set wheel speed
    int bot_distance = 0;


    while (checkInterrupts() != 1 && stop_movement != 1 && bot_distance < (centimeters * 10.65 - 2.50)) {                   //As long as the total distance traveled is less than the
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

    while (checkInterrupts() != 1 && stop_movement != 1 && bot_distance > (centimeters * -10.65 + 2.50)) {
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

    while (checkInterrupts() != 1 && stop_movement != 1 && bot_rotation > degrees * 0.93) {
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

    while (checkInterrupts() != 1 && stop_movement != 1 && bot_rotation < degrees * 0.93) {
        oi_update(sensor);
        bot_rotation += sensor -> angle;
    }
    botAngle += bot_rotation / 0.93;

    oi_setWheels(0, 0);
}

void move(){
    if(direction[0] == 1){
        move_forward(10);
    }else if(direction[1] == 1){
        turn_left(30);
    }else if(direction[2] == 1){
        move_backward(10);
    }else if(direction[3] == 1){
        turn_right(30);
    }
}

int checkInterrupts(){
//  int i;

//  for(i = 0; i < NUMINTERRUPTS; i++){
//      if(sensor_interrupts[i] == 1) return 1;
//  }

    if ( (sensor -> bumpLeft) || (sensor -> bumpRight) || (sensor -> wheelDropLeft) || (sensor -> wheelDropRight) || (sensor -> bumpLeft) || (sensor -> bumpRight) || (sensor -> cliffLeft) || (sensor -> cliffFrontLeft) || (sensor -> cliffFrontRight) || (sensor -> cliffRight)) {
       return 1;
    }

    return 0;
}

































