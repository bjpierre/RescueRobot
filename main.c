/**
 * main.c
 */

#include <final.h>

void main(void) {
	oi_t* sensor_data = oi_alloc();
	oi_init(sensor_data);

	adc_init();
	button_init();
	lcd_init();
	movement_init(sensor_data);
	ping_init();
	servo_ping_init();
	uart_init();

	while (1);
}
