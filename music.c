/*
 * music.c
 *
 *  Created on: Apr 20, 2019
 *      Author: ryanjl9
 */

#include <music.h>

struct note song[LENGTH];

void set_song(int num){
	/*
	 * Tetris Theme A
	 */
	if(num == 1){
		SLENGTH = 57;

		QUARTER = 22;
		EIGTH 	= 11;
		HALF 	= 44;
		WHOLE 	= 88;
		DOTTED 	= 33;

		setNote(1, 76, QUARTER);
		setNote(2, 71, EIGTH);
		setNote(3, 72, EIGTH);
		setNote(4, 74, QUARTER);
		setNote(5, 72, EIGTH);
		setNote(6, 71, EIGTH);
		setNote(7, 69, QUARTER);
		setNote(8, 69, EIGTH);
		setNote(9, 72, EIGTH);
		setNote(10, 76, QUARTER);
		setNote(11, 74, EIGTH);
		setNote(12, 72, EIGTH);
		setNote(13, 71, QUARTER);
		setNote(14, 71, EIGTH);
		setNote(15, 72, EIGTH);
		setNote(16, 74, QUARTER);
		setNote(17, 76, QUARTER);
		setNote(18, 72, QUARTER);
		setNote(19, 69, QUARTER);
		setNote(20, 69, QUARTER);
		setNote(21, 0, QUARTER);
		setNote(22, 74, DOTTED);
		setNote(23, 77, EIGTH);
		setNote(24, 81, QUARTER);
		setNote(25, 79, EIGTH);
		setNote(26, 77, EIGTH);
		setNote(27, 76, DOTTED);
		setNote(28, 72, EIGTH);
		setNote(29, 76, QUARTER);
		setNote(30, 74, EIGTH);
		setNote(31, 72, EIGTH);
		setNote(32, 71, QUARTER);
		setNote(33, 71, EIGTH);
		setNote(34, 72, EIGTH);
		setNote(35, 74, QUARTER);
		setNote(36, 76, QUARTER);
		setNote(37, 72, QUARTER);
		setNote(38, 69, QUARTER);
		setNote(39, 69, QUARTER);
		setNote(40, 0, QUARTER);
		setNote(41, 64, HALF);
		setNote(42, 60, HALF);
		setNote(43, 62, HALF);
		setNote(44, 59, HALF);
		setNote(45, 60, HALF);
		setNote(46, 57, HALF);
		setNote(47, 56, HALF);
		setNote(48, 59, HALF);
		setNote(49, 64, HALF);
		setNote(50, 60, HALF);
		setNote(51, 62, HALF);
		setNote(52, 59, HALF);
		setNote(53, 60, QUARTER);
		setNote(54, 64, QUARTER);
		setNote(55, 69, QUARTER);
		setNote(56, 69, QUARTER);
		setNote(57, 68, WHOLE);
	}

	/*
	 *	Megalovania
	 */
	if(num == 2){
		SLENGTH 		= 44;

		QUARTER 		= 32;
		EIGTH 			= 16;
		SIXTEENTH 		= 8;
		DOTTED_EIGTH 	= 24;

		setNote(1, 62, SIXTEENTH);
		setNote(2, 62, SIXTEENTH);
		setNote(3, 74, EIGTH);
		setNote(4, 69, DOTTED_EIGTH);
		setNote(5, 68, SIXTEENTH);
		setNote(6, 0, SIXTEENTH);
		setNote(7, 67, EIGTH);
		setNote(8, 65, EIGTH);
		setNote(9, 62, SIXTEENTH);
		setNote(10, 65, SIXTEENTH);
		setNote(11, 67, SIXTEENTH);
		setNote(12, 60, SIXTEENTH);
		setNote(13, 60, SIXTEENTH);
		setNote(14, 74, EIGTH);
		setNote(15, 69, DOTTED_EIGTH);
		setNote(16, 68, SIXTEENTH);
		setNote(17, 0, SIXTEENTH);
		setNote(18, 67, EIGTH);
		setNote(19, 65, EIGTH);
		setNote(20, 62, SIXTEENTH);
		setNote(21, 65, SIXTEENTH);
		setNote(22, 67, SIXTEENTH);
		setNote(23, 59, SIXTEENTH);
		setNote(24, 59, SIXTEENTH);
		setNote(25, 74, EIGTH);
		setNote(26, 69, DOTTED_EIGTH);
		setNote(27, 68, SIXTEENTH);
		setNote(28, 0, SIXTEENTH);
		setNote(29, 67, EIGTH);
		setNote(30, 65, EIGTH);
		setNote(31, 62, SIXTEENTH);
		setNote(32, 65, SIXTEENTH);
		setNote(33, 67, SIXTEENTH);
		setNote(34, 58, SIXTEENTH);
		setNote(35, 58, SIXTEENTH);
		setNote(36, 74, EIGTH);
		setNote(37, 69, DOTTED_EIGTH);
		setNote(38, 68, SIXTEENTH);
		setNote(39, 0, SIXTEENTH);
		setNote(40, 67, EIGTH);
		setNote(41, 65, EIGTH);
		setNote(42, 62, SIXTEENTH);
		setNote(43, 65, SIXTEENTH);
		setNote(44, 67, SIXTEENTH);
	}
}

void load_song(){
	unsigned char notes[16];
	unsigned char dur[16];
	struct note* temp;

	int i, j;

	for(i = 0; i < 4; i++){
		memset(notes, 0, 16);
		memset(dur, 0, 16);

		temp = getSong(i);

		for(j = 0; j < getSize(i+1); j++){
			notes[j] = temp[j].note;
			dur[j] = temp[j].dur;
		}

		oi_loadSong(i, getSize(i+1), notes, dur);
	}
}

void music_init(int numsong){
	int i;

	for(i = 0; i < LENGTH; i++){
		song[i].note = 0;
		song[i].dur = 0;
	}

	set_song(numsong);
	load_song();
}

int getSize(int num){
	if(SLENGTH/16*num) return 16;
	else return(SLENGTH - 16*(num-1));
}

struct note* getSong(int num){
	return &(song[num*16]);
}

void setNote(int index, int note, int dur){
	song[index-1].note = note;
	song[index-1].dur = dur;
}






















