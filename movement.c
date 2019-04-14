/*
 * movement.c
 *
 *  Created on: Feb 7, 2019
 *      Author: ryanjl9
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

	uint32_t temp[NUMINTERRUPTS] = {
		s -> wheelDropLeft,
		s -> wheelDropRight,
		s -> bumpLeft,
		s -> bumpRight,
		s -> cliffLeft,
		s -> cliffFrontLeft,
		s -> cliffFrontRight,
		s -> cliffRight
	};

	memcpy(sensor_interrupts, temp, NUMINTERRUPTS);
}

/**
 * move_forward: This method moves the CyBot forward
 */
void move_forward() {
	oi_setWheels(300, 300);							//Set wheel speed

	while (checkInterrupts() != 1 && stop_movement != 1) {					//As long as the total distance traveled is less than the
		oi_update(sensor);							//desired, keep going
	}

	oi_setWheels(0, 0);
}

/**
 * move_backwards: This method moves the CyBot backwards
 */
void move_backwards() {
	oi_setWheels(-300, -300);

	while (checkInterrupts() != 1 && stop_movement != 1) {
		oi_update(sensor);
	}
}

/**
 * turn_right: This method turns the CyBot right
 */
void turn_right() {
	oi_setWheels(-200, 200);

	while (checkInterrupts() != 1 && stop_movement != 1) {
		oi_update(sensor);
	}

	oi_setWheels(0, 0);
}

/**
 * turn_left: This method turns the CyBot left
 */
void turn_left() {
	oi_setWheels(200, -200);						//Sets wheel speeds to opposites so the roomba spins left

	while (checkInterrupts() != 1 && stop_movement != 1) {
		oi_update(sensor);
	}

	oi_setWheels(0, 0);
}

void move(){
	if(direction[0] == 1){
		move_forward();
	}else if(direction[1] == 1){
		turn_left();
	}else if(direction[2] == 1){
		move_backwards();
	}else if(direction[3] == 1){
		turn_right();
	}
}

int checkInterrupts(){
	int i;

	for(i = 0; i < NUMINTERRUPTS; i++){
		if(sensor_interrupts[i] == 1) return 1;
	}
	return 0;
}

































