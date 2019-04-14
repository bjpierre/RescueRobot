/*
 * ping.c
 *
 *  Created on: Mar 14, 2019
 *      Author: ryanjl9
 */
#include <ping.h>

int init_value;											//Variable to store inital clock value
int timer_value;										//Global variable to store final clock valued

/**
 * ping_distance: Stores the distance of the ping sensor in cm
 */
double ping_distance;

/**
 * ping_init: This method initializes the ping sensor
 *
 * CALL THIS METHOD
 */
void ping_init() {
	init_value = 0;										//Initalizes the init_value, timer_value, and ping_distance
	timer_value = 0;
	ping_distance = 0;

	SYSCTL_RCGCGPIO_R |= 0x02;							//Enables clock for GPIO port B and Timer 3
	SYSCTL_RCGCTIMER_R |= 0x08;

	GPIO_PORTB_DIR_R |= 0x08;							//Sets port 3 in input and digital enable to true
	GPIO_PORTB_DEN_R |= 0x08;

	GPIO_PORTB_AFSEL_R |= 0x08;							//Sets pin 3 alternate function to true

	GPIO_PORTB_PCTL_R |= 0x00007000;						//Sets pin 3 alternate function to timer 3

	timer3b_init();										//Sets up timer variables
}

/**
 * timer3b_init: This method sets up the timer 3b portion
 */
void timer3b_init() {
	TIMER3_CTL_R &= 0xFEFF;								//Disables timer 3
	TIMER3_CFG_R = 0x4;									//Selects timer 3 16 bit
	TIMER3_TBMR_R |= 0x0017;							//Sets to capture mode, edge timing, and count up
	TIMER3_CTL_R |= 0x0C00;								//Both edge sensing
	TIMER3_TBPR_R = 0x00FF;								//Set 8 bit prescaler
	TIMER3_TBILR_R = 0x0FFF;							//Set max number to 0xFFF
	TIMER3_IMR_R |= 0x0400;								//Sets interrupts for capture mode event to true

	NVIC_EN1_R |= 0x00000010;							//Sets interrupts for timer 3

	IntRegister(INT_TIMER3B, ping_interrupt);			//Links function to interrupt

	IntMasterEnable();
}

/**
 * ping_getDistance: This method sends a trigger to the ping module
 *
 * CALL THIS METHOD
 */
void ping_getDistance() {
	if(init_value == 0){								//Checks if first measurement has been taken
		TIMER3_CTL_R &= 0xFEFF;							//Disables timer

		TIMER3_TBR_R = 0;								//Sets timer reg to 0

		GPIO_PORTB_AFSEL_R &= 0xF7;						//Sets alternate function for pin 3 to false
		GPIO_PORTB_DIR_R |= 0x08;						//Sets pin 3 direction to output

		GPIO_PORTB_DATA_R &= 0xF7;						//Writes low on pin 3
		timer_waitMicros(5);
		GPIO_PORTB_DATA_R |= 0x08;						//Writes high on pin 3
		timer_waitMicros(5);
		GPIO_PORTB_DATA_R &= 0xF7;						//Writes low on pin 3

		GPIO_PORTB_AFSEL_R |= 0x08;						//Sets alternate function for pin 3 to true
		GPIO_PORTB_DIR_R &= 0xF7;						//Sets direction of pin 3 to input

		TIMER3_CTL_R |= 0x0100;							//Enables timer
	}
}

/**
 * ping_interrupt: This method handles the interrupts generated by the ping module
 */
void ping_interrupt() {
	TIMER3_ICR_R |= 0x0400;								//Clears interrupt

	int value = TIMER3_TBR_R;							//Stores timer 3 value in local value variable

	if(init_value == 0){								//If leading edge, continue in if statement, else fall through
		init_value = value;								//Assigns inital value to value
		return;
	}

	timer_value = value - init_value;					//Set timer_value to the difference between current timer value and inital value
	dist(timer_value);									//Refreshes the ping_distance variable
	init_value = 0;										//Set init_value to 0

	return;
}

/**
 * dist: This method calculates the distance from an object
 */
void dist(int val){
	ping_distance = (val / (32000.0))*340;
}



