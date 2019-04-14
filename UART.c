/*
 * UART.c
 *
 *  Created on: Feb 21, 2019
 *      Author: ryanjl9
 */

#include <UART.h>

#define BIT0        0x01
#define BIT1        0x02
#define BIT2        0x04
#define BIT3        0x08
#define BIT4        0x10
#define BIT5        0x20
#define BIT6        0x40
#define BIT7        0x80


char data;
char message[21];
int len;
int distance = 0;

/**
 * uart_init: This method is used to initialize the uart module
 *
 * CALL THIS METHOD
 */
void uart_init(){
	SYSCTL_RCGCGPIO_R |= 0x02;
	SYSCTL_RCGCUART_R |= 0x02;

	GPIO_PORTB_AFSEL_R |= (BIT0 | BIT1);
	GPIO_PORTB_PCTL_R |= 0x00000011;

	GPIO_PORTB_DEN_R |= (BIT0 | BIT1);
	GPIO_PORTB_DIR_R &= 0xFE;
	GPIO_PORTB_DIR_R |= 0x02;

	UART1_CTL_R &= ~UART_CTL_UARTEN;

	//104. 11
	//8, 44

	UART1_IBRD_R = 8;
	UART1_FBRD_R = 44;

	UART1_CC_R = 0;

	UART1_LCRH_R = 0x60;

	UART1_ICR_R |=0x0030;
	UART1_IM_R |= 0x0010;

	//6
	NVIC_PRI1_R |= 0x00002000;
	NVIC_EN0_R |= 0x00000040;

	IntRegister(INT_UART1, uart_handler);

	UART1_CTL_R = 0x0301;

	IntMasterEnable();
}

/**
 * uart_sendChar: This method is used to send data to putty
 */
void uart_sendChar(char ldata){
	UART1_DR_R = ldata;
}

/**
 * uart_recieve: This method is used to recieve data from putty
 */
char uart_recieve(){
	return (char)(UART1_DR_R & 0xFF);
}

/**
 * uart_handler: This method is used to handle interrupts involving uart
 */
void uart_handler(){
	data = uart_recieve();

	if(data > 31 && data < 127){
		message[len] = data;
		len++;
	}

	if(data == 127 && len > 0) message[--len] = '\0';

	if(len==20 || data==13){
		lcd_clear();

		message[len] = '\0';
		len++;

		lcd_printf("%s", message);

		memset(message, 0, len);
		len = 0;
	}

	UART1_ICR_R |=0x0030;
}

/**
 * print: This method is used to send information back to putty formatted
 *
 * CALL THIS METHOD
 */
void print(char* dir, int dist){
    char mes[21] = {0};
    char* buffer;
    int i;
    strcat(mes, "DIR: ");
    strcat(mes, dir);
    strcat(mes, "\r\nDIST: ");
    asprintf(&buffer, "%i", dist);
    strcat(mes, buffer);
    strcat(mes, "\0");

    for(i = 0; i < strlen(mes); i++){
        uart_sendChar(mes[i]);
        timer_waitMillis(10);
    }
    uart_sendChar('\r');
    uart_sendChar('\n');
}





















