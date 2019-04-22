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

	botX = 0;
	botY = 0;
	botAngle = 0;
	char sendStr[60] = { 0 };

	turn_left(30);
	sprintf(sendStr, ".a%d.x%d.y%d", botAngle, botX, botY);
	uart_sendStr(sendStr);
//	while (1);

	oi_free(sensor_data);
}
