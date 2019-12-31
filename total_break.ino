#include "total_break.h"
#include <SoftwareSerial.h>
#include <Arduino.h>
#include <U8g2lib.h>
#ifdef U8X8_HAVE_HW_SPI
#include <SPI.h>
#endif

#ifdef U8X8_HAVE_HW_I2C
#include <Wire.h>
#endif
U8G2_SH1106_128X64_NONAME_1_HW_I2C u8g2(U8G2_R0, /* reset=*/ U8X8_PIN_NONE);

#define Interrupt0 2  // 인터럽트 입력 핀 
#define Interrupt1 3

#define touchSensor1 4  //버튼 , 터치센서 이벤트 입력
#define button1      5
#define touchSensor2 6

#define outof_Interrupt0 7 // 인터럽트 발생용 출력
#define outof_Interrupt1 8

#define Rx 10
#define Tx 11
SoftwareSerial btSerial(Rx, Tx);

bool t_val_1 = false;
bool t_val_2 = false;
bool b_val_0 = false;

int menu_state = 3;
int wight_cursor = 10;
String temp_str[3];
String command[3];

void setup() {

	pinMode(Interrupt0, INPUT_PULLUP); // 인터럽트 입력용 핀 설정
	//pinMode(Interrupt1, INPUT_PULLUP);

	pinMode(touchSensor1, INPUT); //아두이노 제어용 센서 INPUT설정
	pinMode(button1, INPUT_PULLUP);
	pinMode(touchSensor2, INPUT);


	pinMode(outof_Interrupt0, OUTPUT); //인터럽트 제어용 출력포트 설정
	digitalWrite(outof_Interrupt0, LOW);
	pinMode(outof_Interrupt1, OUTPUT);

	//attachInterrupt(digitalPinToInterrupt(Interrupt0), do_rellol, RISING); //Interrupt0를 인터럽트 번호로 변경 , 실행할 함수, 실행조건
	//attachInterrupt(1, 미정, CHANGE); //인터럽트 활성화


	u8g2.begin();
	u8g2.enableUTF8Print();		// enable UTF8 support for the Arduino print() function
	u8g2.setFont(u8g2_font_unifont_t_korean1);

	u8g2.setFontDirection(0);

	Serial.begin(9600);
	btSerial.begin(9600);
}

void loop() {
	touchSensor_listener();
	//button_push();
	class_sep_display(menu_state);
	bt_rx();
}

void bt_rx() {
	if (btSerial.available()) {
		for (size_t i = 0; i < 3; i++) {
			command[i] = "";
		}
		for (size_t i = 0; i < 3; i++) {
			if (command[i].equals("")) {
				command[i] = btSerial.readStringUntil('\n');
			}
		}
		for (size_t i = 0; i < 3; i++) {
			Serial.println(command[i]);
		}
	}
	if (command[0][0] == 'c' && command[0][1] == 'o' && command[0][2] == 'm' && command[0][3] == 'm' && command[0][4] == 'a' && command[0][5] == 'n' && command[0][6] == 'd') {
		switch (command[1][2]) {
		case 'n': {
			btSerial.println((String)menu_state + ",next");
			Serial.println((String)menu_state + ",next");
			wight_cursor = 10;

			break;
		}
		case 'p': {
			btSerial.println((String)menu_state + ",prev");
			Serial.println((String)menu_state + ",prev");
			wight_cursor = 10;

			break;

		}
		case 'm': {
			menu_state = (menu_state + 1) % 4;
			btSerial.println((String)menu_state + ",start");
			Serial.println((String)menu_state + ",start");
			wight_cursor = 10;

			break;
		}
		case 'R': {
			do_rellol();
			break;

		}
		case 'E': {
			Serial.println("음성인식 오류");
			break;
		}
		
		default: {
			Serial.println("ERROR");
			break;
		}

		}
		for (size_t i = 0; i < 3; i++) {
			command[i] = "";
		}
	}
	else
	{
		for (size_t i = 0; i < 3; i++)
		{
			temp_str[i] = command[i];
		}
	}

}

void touchSensor_listener() {
	while (digitalRead(touchSensor1) || digitalRead(touchSensor2) || digitalRead(button1)) {
		if (digitalRead(touchSensor1))
			t_val_1 = true;  //터치가 한번이라도 됫으면 True 로 설정
		if (digitalRead(touchSensor2))
			t_val_2 = true;
		if (digitalRead(button1))
			b_val_0 = true;
		Serial.println("추가입력 대기중");
		delay(500);
	}

	if (t_val_1 && t_val_2 && !b_val_0) {  // 둘다 true 일때
		Serial.println("3");
		do_rellol();
		wight_cursor = 10;
		t_val_1 = false;
		t_val_2 = false;
		b_val_0 = false;


		//새로고침 인터럽트
		/*
		digitalWrite(outof_Interrupt0, HIGH);
		delay(500);
		digitalWrite(outof_Interrupt0, LOW);
		*/
	}

	else if (t_val_1 && !t_val_2 && !b_val_0) { // 1번만 true 일때
		Serial.println("1");
		btSerial.println((String)menu_state + ",prev");
		Serial.println((String)menu_state + ",prev");
		wight_cursor = 10;


		t_val_1 = false;
		t_val_2 = false;
		b_val_0 = false;
	}
	else if (!t_val_1 && t_val_2 && !b_val_0) {  // 2 번만 true 일때
		Serial.println("2");
		btSerial.println((String)menu_state + ",next");
		Serial.println((String)menu_state + ",next");
		wight_cursor = 10;

		t_val_1 = false;
		t_val_2 = false;
		b_val_0 = false;
	}

	//버튼만 눌렷을때
	else if (b_val_0 && !t_val_1 && !t_val_2) {
		Serial.println("버튼눌림");
		while (true) {
			if (!digitalRead(button1))
				break;
		}
		menu_state = (menu_state + 1) % 4;
		btSerial.println((String)menu_state + ",start");
		Serial.println((String)menu_state + ",start");
		t_val_1 = false;
		t_val_2 = false;
		b_val_0 = false;
	}

	//3개 다 눌렷을때
	else if (b_val_0 && t_val_1 && t_val_2) {
		Serial.println("3개눌림");
		Serial.println("음성인식");
		btSerial.println("voice_rec");
		t_val_1 = false;
		t_val_2 = false;
		b_val_0 = false;
	}

}

/*
//메뉴 제어용 버튼이 눌렸을때
void button_push() {
	if (digitalRead(button1)) {
		Serial.println("버튼눌림");
		while (true) {
			if (!digitalRead(button1))
				break;
		}
		menu_state = (menu_state + 1) % 4;
		btSerial.println((String)menu_state + ",start");
		Serial.println((String)menu_state + ",start");
	}
}
*/

// 새로고침 함수
void do_rellol() {
	btSerial.println("Refresh");
	Serial.println("새로고침 인터럽트");
}


//디스플레이 제어
// switch-case문으로
void class_sep_display(int menu_state) {
	u8g2.firstPage();
	u8g2.clearBuffer();
	wight_cursor--;


	switch (menu_state) {
	case 0: { //시간
		if (wight_cursor < -128)
			wight_cursor = 10;
		do {
			u8g2.setCursor(0, 15);
			u8g2.println(temp_str[0]);

			u8g2.setCursor(0, 35);
			u8g2.print(temp_str[1]);


			u8g2.setCursor(wight_cursor, 55);
			u8g2.print(temp_str[2]);

		} while (u8g2.nextPage());
		break;
	}

	case 1: { // 버스
		do {
			u8g2.setCursor(0, 15);
			for (int j = 0; j < temp_str[0].length(); j++) {
				if (j > 48) {
					break;
				}
				u8g2.print(temp_str[0][j]);
			}


			u8g2.setCursor(0, 35);
			for (int j = 0; j < temp_str[1].length(); j++) {
				if (j > 48) {
					break;
				}
				u8g2.print(temp_str[1][j]);
			}

			u8g2.setCursor(0, 55);
			for (int j = 0; j < temp_str[2].length(); j++) {
				if (j > 48) {
					break;
				}
				u8g2.print(temp_str[2][j]);
			}
		} while (u8g2.nextPage());
		break;
	}

	case 2: { //길찾기
		if (wight_cursor < -128)
			wight_cursor = 10;
		do {
			u8g2.setCursor(35, 25);
			u8g2.println("(" + temp_str[0] + "/" + temp_str[1] + ")");

			u8g2.setCursor(wight_cursor, 55);
			for (int j = 0; j < temp_str[2].length(); j++) {
				if (j > 40) {
					break;
				}
				u8g2.print(temp_str[2][j]);
			}
		} while (u8g2.nextPage());
		break;
	}

	case 3: { //날씨
		if (wight_cursor < -40)
			wight_cursor = 10;
		do {


			u8g2.setCursor(wight_cursor, 15);
			for (int j = 0; j < temp_str[0].length(); j++) {
				if (j > 48) {
					break;
				}
				u8g2.print(temp_str[0][j]);
			}


			u8g2.setCursor(wight_cursor, 35);
			for (int j = 0; j < temp_str[1].length(); j++) {
				if (j > 48) {
					break;
				}
				u8g2.print(temp_str[1][j]);
			}

			u8g2.setCursor(wight_cursor, 55);
			for (int j = 0; j < temp_str[2].length(); j++) {
				if (j > 48) {
					break;
				}
				u8g2.print(temp_str[2][j]);
			}
		} while (u8g2.nextPage());
		break;
	}
	default:
		Serial.println("니가 왜나와...?");
		break;
	}

}
