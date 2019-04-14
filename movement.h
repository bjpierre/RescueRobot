/*
 * movement.h
 *
 *  Created on: Feb 7, 2019
 *      Author: ryanjl9
 */

#ifndef MOVEMENT_H_
#define MOVEMENT_H_

#include "open_interface.h"

void movement_init(oi_t* s);
void move_forward();
void turn_right();
void turn_left();
void move_backwards();
void move();
int checkInterrupts();

extern int direction[];
extern int stop_movement;

#endif /* MOVEMENT_H_ */
