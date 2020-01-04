#define LEFT_AHEAD 12
#define LEFT_BACK 13
#define RIGHT_AHEAD 10
#define RIGHT_BACK 9
#define FORWARD_AHEAD 14
#define FORWARD_BACK 19

int duty = 1230;
String inString="";

int TURN = 100;
int RUN1 =135;
int RUN2 = 150;
int CYC = 5;


void stopBack();
void turnLeft();
void turnRight();
void goAhead();
void park();
void goBack();

void setup(){
    Serial.begin(9600);
    pinMode(LEFT_AHEAD,OUTPUT);
    pinMode(LEFT_BACK, OUTPUT);
    pinMode(RIGHT_AHEAD, OUTPUT);
    pinMode(RIGHT_BACK, OUTPUT);
    pinMode(FORWARD_AHEAD, OUTPUT);
    pinMode(FORWARD_BACK, OUTPUT);
    digitalWrite(LEFT_AHEAD, LOW);
    digitalWrite(LEFT_BACK, LOW);
    digitalWrite(RIGHT_AHEAD, LOW);
    digitalWrite(RIGHT_BACK, LOW);;
    digitalWrite(FORWARD_AHEAD, LOW);
    digitalWrite(FORWARD_BACK, LOW);
}

char incomingByte = ' ';

void loop(){
    if (Serial.available() > 0) {
        // read the incoming byte:
        incomingByte = Serial.read();
        if (incomingByte == 'L') {
            //Serial.println("TURN LEFT");
            turnLeft();
            Serial.read();
        } else if (incomingByte == 'R'){
           // Serial.println("TURN RIGHT");
            turnRight();
            Serial.read();
        } else if (incomingByte == 'A'){
            //Serial.println("GO AHEAD");
            goAhead();
            Serial.read();
        } else if (incomingByte == 'B'){
            //Serial.println("GO BACK");
            goBack();
            Serial.read();
        } else if (incomingByte == 'l') {
           // Serial.println("TURN LEFT and STOP");
            turnLeft();
            delay(3000);
            park();
            Serial.read();
        } else if (incomingByte == 'r'){
            //Serial.println("TURN RIGHT and STOP");
            turnRight();
            delay(3000);
            park();
            Serial.read();
        } else if (incomingByte == 'a'){
            //Serial.println("GO AHEAD and STOP");
            goAhead();
            delay(3000);
            park();
            Serial.read();
        } else if (incomingByte == 'b'){
           // Serial.println("GO BACK and STOP");
            goBack();
           delay(3000);
            park();
            Serial.read();
        } else if (incomingByte == 'P'){
            //Serial.println("PARK");
            park();
            Serial.read();
        } 
        delay(1000);
    }
}


void goAhead(){
    park();
    analogWrite(LEFT_AHEAD, RUN2);  
    analogWrite(RIGHT_BACK, RUN1);
}

void turnLeft(){
    park();
    analogWrite(RIGHT_AHEAD,TURN);
    digitalWrite(RIGHT_BACK,LOW);
    analogWrite(LEFT_AHEAD,TURN);
    digitalWrite(LEFT_BACK,LOW);
    analogWrite(FORWARD_AHEAD, TURN);
    digitalWrite(FORWARD_BACK, LOW);
}

void turnRight(){
    park();
    //stopBack();
    analogWrite(LEFT_BACK,TURN);
    digitalWrite(LEFT_AHEAD,LOW);
    analogWrite(RIGHT_BACK,TURN);
    digitalWrite(RIGHT_AHEAD,LOW);
    analogWrite(FORWARD_BACK,TURN);
    digitalWrite(FORWARD_AHEAD,LOW);
    
}

void park(){
    stopBack();
    digitalWrite(LEFT_AHEAD, LOW);
    digitalWrite(RIGHT_AHEAD, LOW);
    digitalWrite(FORWARD_AHEAD, LOW);
}

void goBack(){
    park();
    analogWrite(LEFT_BACK, RUN2);
    analogWrite(RIGHT_AHEAD,RUN1);  
}

void stopBack(){
    digitalWrite(LEFT_BACK, LOW);
    digitalWrite(RIGHT_BACK, LOW);
    digitalWrite(FORWARD_BACK, LOW);
}
