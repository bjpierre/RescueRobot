/*
 * object_detection.h
 *
 *  Created on: Apr 7, 2019
 *      Author: ryanjl9
 */

#ifndef OBJECT_DETECTION_H_
#define OBJECT_DETECTION_H_

#include <stdio.h>
#include <math.h>

struct point{
	int degree;
	double distIR;
	double distSonar;
};

struct object{
	int rSize;

	double lWidth;

	struct point data[20];
};

extern struct object objects[];
extern struct point points[];

extern int numObjects;

void getObjects();
void getLinearWidth(struct object* obj);
void sweep();

#endif /* OBJECT_DETECTION_H_ */
