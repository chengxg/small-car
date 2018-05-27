#include <MsTimer2.h>
#include <Servo.h>

//定时器对应的 pwm引脚
// T/C0: Pin6(OC0A)和Pin5(OC0B)
// T/C1: Pin9(OC1A)和Pin10(OC1B)
// T/C3: Pin11(OC2A)和Pin3(OC2B)

//pwm输出引脚 3, 5, 6, 9, 10, 11
//驱动电机
const byte motor_pin_1 = 6;
const byte motor_pin_2 = 5;

//定义转向舵机  舵机接口（9、10 都可以，缺点只能控制2 个）
const byte dir_servo_pin = 9;
Servo dirServo; //定义转向舵机

//红外线避障模块, 从左至右 1,2,3
const byte infrared1_pin = 4;
const byte infrared2_pin = 7;
const byte infrared3_pin = 8;

//自动避障 设定SR04连接的Arduino引脚
const byte avoiding_trig_pin = 12;
const byte avoiding_echo_pin = 13;
//定义避障舵机
const byte avoiding_servo_pin = 10;
Servo avoidingServo;

//电机控制常量
const byte MOTOR_FORWARD = 2; //前进
const byte MOTOR_BACK = 0;    //后退
const byte MOTOR_STOP = 1;    //停止

//转向控制常量
const byte TURN_LEFT = 1;     //左转
const byte TURN_MIDDLE = 0;   //直行
const byte TURN_RIGHT = 2;    //右转
const byte TURN_MAX_DEG = 60; //小车最大转弯角度

//小车模式
const byte CARMODE_AUTO_TRACE = 2;     //自动寻迹 模式
const byte CARMODE_MANUAL_CONTROL = 1; //手动遥控 模式
const byte CARMODE_AUTO_AVOIDING = 3;  //自动避障 模式
byte carMode = CARMODE_AUTO_TRACE;  //当前小车的模式

//定时器副线程 计数参数
unsigned int timeCount = 0;

//寻迹模式 参数
byte atTurnMaxDeg = TURN_MAX_DEG;
byte atMaxSpeed = 140;         //自动寻迹电机最大速度
byte atLastInfraredStatus = 0; //红外寻迹历史记录

//手动遥控模式 参数
byte mcMotorSpeed = 0;      //电机速度
byte mcSpeedDir = 0;        //电机状态
byte mcTurnDeg = 0;         //转弯角度
byte mcTurnDir = 0;         //转弯方向
long mcLastReceiveTime = 0; //记录接收到遥控信号的时间

//自动避障 参数
byte avServoMiddleDeg = 90;  //舵机中值角度                                     //避障舵机摆正角度
byte avMeasureDegArr[] = {20, 45, 60, 75, 90, 105, 120, 135, 160}; //测量的角度
byte avMeasureDistanceArr[] = {0, 0, 0, 0, 0, 0, 0, 0, 0};         //对应角度处的测量距离
boolean avTurnDir = true;                                          //云台旋转方向, 从左至右为true
//此处代替 delay(xxx) 等耗时程序
//为什么不用delay(xxx)呢? 因为此方法 每15ms进入一次, 耗时过大, 会造成程序阻塞
unsigned int avDelayTimeCount = 0; //定时器循环延时计数
boolean avIsFirstDelay = false;    //是否执行第一个耗时程序块
boolean avIsSencondDelay = false;  //是否执行第二个耗时程序块
//记录距离
unsigned int avRightAvgDistance = 0;  //右边的平均距离
unsigned int avLeftAvgDistance = 0;   //左边的平均距离
unsigned int avMiddleAvgDistance = 0; //中间的平均距离


//-----------------------------小车状态控制 开始-------------------------
//电机控制
//前进后退, 速度
void motorControl(byte motorStatus, byte speed)
{
    switch (motorStatus)
    {
    case MOTOR_FORWARD:
        analogWrite(motor_pin_1, speed);
        digitalWrite(motor_pin_2, LOW);
        break;
    case MOTOR_STOP:
        analogWrite(motor_pin_1, 0);
        digitalWrite(motor_pin_2, LOW);
        break;
    case MOTOR_BACK:
        digitalWrite(motor_pin_1, LOW);
        analogWrite(motor_pin_2, speed);
        break;
    }
}

//转向舵机 控制
//左右 , 角度
void dirServoControl(byte turnStatus, byte deg)
{
    if (deg > TURN_MAX_DEG)
    {
        deg = TURN_MAX_DEG;
    }

    switch (turnStatus)
    {
    case TURN_LEFT:
        dirServo.write(90 - deg);
        break;
    case TURN_MIDDLE:
        dirServo.write(90);
        break;
    case TURN_RIGHT:
        dirServo.write(90 + deg);
        break;
    }
}
//-----------------------------小车状态控制 结束-------------------------

//--------------------------自动寻迹 开始-------------------------
//自动寻迹控制
void autoTraceControl()
{
    //红外寻迹模块
    //0: 亮, 1: 灭
    //0: 白, 1: 黑
    byte infrared1 = digitalRead(infrared1_pin);
    byte infrared2 = digitalRead(infrared2_pin);
    byte infrared3 = digitalRead(infrared3_pin);
    byte status = infrared1 * 4 + infrared2 * 2 + infrared3 * 1;

    switch (status)
    {
    //0,0,0
    //小车直行
    case 0:
        dirServoControl(TURN_MIDDLE, 0);
        motorControl(MOTOR_FORWARD, atMaxSpeed - 20);
        break;
    case 1:
        autoTraceTurn(1);
        break;
    //快速直行
    case 2:
        dirServoControl(TURN_MIDDLE, 0);
        motorControl(MOTOR_FORWARD, atMaxSpeed);
        break;
    case 3:
        autoTraceTurn(3);
        break;
    case 4:
        autoTraceTurn(4);
        break;
    //101 继续保持上一个状态
    case 5:
        break;
    case 6:
        autoTraceTurn(6);
        break;
    //小车停止
    case 7:
        dirServoControl(TURN_MIDDLE, 0);

        if (atLastInfraredStatus != 7)
        {
            //电机倒转 紧急刹车
            motorControl(MOTOR_BACK, 200);
            delay(20);
        }
        motorControl(MOTOR_STOP, 0);
        break;
    }
}

//自动寻迹转弯动作
void autoTraceTurn(byte startInfraredStatus)
{
    byte turnStatus = 0;
    if (startInfraredStatus == 1 || startInfraredStatus == 3)
    {
        turnStatus = TURN_RIGHT;
    }
    if (startInfraredStatus == 4 || startInfraredStatus == 6)
    {
        turnStatus = TURN_LEFT;
    }
    if (turnStatus == 0)
    {
        return;
    }

    byte infrared1 = 0;
    byte infrared2 = 0;
    byte infrared3 = 0;

    //初始转弯状态
    byte startTurnDeg = 10;
    byte startSpeed = atMaxSpeed - 40;
    dirServoControl(turnStatus, startTurnDeg);
    motorControl(MOTOR_FORWARD, startSpeed);

    long t = millis();
    int count = 0;
    //转弯, 直到状态改变
    while (true)
    {
        infrared1 = digitalRead(infrared1_pin);
        infrared2 = digitalRead(infrared2_pin);
        infrared3 = digitalRead(infrared3_pin);

        long ct = millis();

        //缓慢改变小车状态, 每10ms变一次
        if ((ct - t) > 15)
        {
            count++;
            t = ct;
            if (startTurnDeg <= atTurnMaxDeg)
            {
                startTurnDeg = startTurnDeg + count / 5 + 1;
                if (startTurnDeg <= atTurnMaxDeg)
                {
                    dirServoControl(turnStatus, startTurnDeg);
                }
            }

            if (startSpeed <= atMaxSpeed)
            {
                startSpeed = startSpeed + 2;
                if (startSpeed <= atMaxSpeed)
                {
                    motorControl(MOTOR_FORWARD, startSpeed);
                }
            }
        }

        //右转 001
        if (startInfraredStatus == 1)
        {
            if (infrared2)
            {
                break;
            }
            if (infrared1)
            {
                break;
            }
        }
        //右转 011
        if (startInfraredStatus == 3)
        {
            if (infrared1)
            {
                break;
            }
            if (!infrared3 && infrared2)
            {
                break;
            }
        }

        //左转 100
        if (startInfraredStatus == 4)
        {
            if (infrared2)
            {
                break;
            }
            if (infrared3)
            {
                break;
            }
        }
        //左转 110
        if (startInfraredStatus == 6)
        {
            if (infrared3)
            {
                break;
            }
            if (!infrared1 && infrared2)
            {
                break;
            }
        }
    }
    delay(20); //保持转弯状态再转20ms
}
//--------------------------自动寻迹 结束-------------------------

//--------------------------自动避障 开始-------------------------
//超声波测距
unsigned int distanceMeasurement()
{
    // 产生一个10us的高脉冲去触发TrigPin
    digitalWrite(avoiding_trig_pin, LOW);
    delayMicroseconds(2);
    digitalWrite(avoiding_trig_pin, HIGH);
    delayMicroseconds(10);
    digitalWrite(avoiding_trig_pin, LOW);
    // 检测脉冲宽度，并计算出距离
    unsigned long distance = pulseIn(avoiding_echo_pin, HIGH) / 58.00;
    if (distance == 0)
    {
        distance = 5;
    }
    if (distance > 2000)
    {
        distance = 2000;
    }
    return distance;
}

//主线程 循环测量周围距离
void loopDistanceMeasure2()
{
    byte currentDeg = avoidingServo.read(); //当前角度
    //查找出离当前位置 最近的 测量角度
    byte minDiffDeg = abs(avMeasureDegArr[0] - currentDeg);
    byte currentDegIndex = 0; //测量角度的索引
    byte len = sizeof(avMeasureDegArr) / sizeof(byte);
    for (byte i = 0; i < len; i++)
    {
        byte temp = abs(avMeasureDegArr[i] - currentDeg);
        if (minDiffDeg > temp)
        {
            minDiffDeg = temp;
            currentDegIndex = i;
        }
    }
    //与测量角度处有偏差 则 舵机 转到 测量角度处
    if (minDiffDeg > 0)
    {
        currentDeg = avMeasureDegArr[currentDegIndex];
        avoidingServo.write(currentDeg);
        delay(minDiffDeg * 3);
    }

    //测量距离, 此处最为耗时
    avMeasureDistanceArr[currentDegIndex] = distanceMeasurement();

    Serial.print("dis:");
    Serial.print(currentDegIndex);
    Serial.print(" ");
    Serial.println(avMeasureDistanceArr[currentDegIndex]);

    //转到头 换舵机旋转方向
    if (currentDegIndex <= 0)
    {
        avTurnDir = true;
    }
    if (currentDegIndex >= len - 1)
    {
        avTurnDir = false;
    }

    //设置下一个测量角度
    if (avTurnDir)
    {
        byte nextDeg = avMeasureDegArr[currentDegIndex + 1];
        avoidingServo.write(nextDeg);
        delay((nextDeg - currentDeg) * 3);
    }
    else
    {
        byte nextDeg = avMeasureDegArr[currentDegIndex - 1];
        avoidingServo.write(nextDeg);
        delay((currentDeg - nextDeg) * 3);
    }
}

//副线程 自动避障控制
void autoAvoidingControl2()
{
    if (avDelayTimeCount > 0)
    {
        //存在延时则跳出程序
        //延时时间 t = avDelayTimeCount*15;
        avDelayTimeCount--;
        if (!(avIsSencondDelay && avDelayTimeCount == 0))
        {
            return;
        }
    }
    else
    {
        avIsFirstDelay = true;
        avIsSencondDelay = true;

        //计算平均值
        unsigned int rightDistanceSum = avMeasureDistanceArr[6] + avMeasureDistanceArr[7] + avMeasureDistanceArr[8];
        unsigned int leftDistanceSum = avMeasureDistanceArr[0] + avMeasureDistanceArr[1] + avMeasureDistanceArr[2];
        unsigned int middleDistanceSum = avMeasureDistanceArr[3] + avMeasureDistanceArr[4] + avMeasureDistanceArr[5];

        avRightAvgDistance = rightDistanceSum / 3;
        avLeftAvgDistance = leftDistanceSum / 3;
        avMiddleAvgDistance = middleDistanceSum / 3;
    }

    //初始时还未完全测量完一周, 退出
    if (avMiddleAvgDistance == 0 || avRightAvgDistance == 0 || avLeftAvgDistance == 0)
    {
        return;
    }

    //控制小车前进方向
    //空间不够 后退
    if (avMiddleAvgDistance < 15 || avRightAvgDistance < 10 || avLeftAvgDistance < 10)
    {
        if (avIsFirstDelay)
        {
            avIsFirstDelay = false;
            motorControl(MOTOR_BACK, 100);
            dirServoControl(TURN_MIDDLE, 0);
            avDelayTimeCount = 80; //延时 1200ms
        }
        return;
    }

    //空间足够 前进
    if (avMiddleAvgDistance > 35 && avRightAvgDistance > 25 && avLeftAvgDistance > 25)
    {
        motorControl(MOTOR_FORWARD, 120);
        dirServoControl(TURN_MIDDLE, 0);
        return;
    }

    //左转
    if (avRightAvgDistance > avLeftAvgDistance)
    {
        if (avIsFirstDelay)
        {
            avIsFirstDelay = false;
            dirServoControl(TURN_LEFT, TURN_MAX_DEG - 5);
            motorControl(MOTOR_BACK, 200);
            avDelayTimeCount = 30; //延时 450ms
            return;
        }

        if (avIsSencondDelay)
        {
            avIsSencondDelay = false;
            dirServoControl(TURN_RIGHT, TURN_MAX_DEG - 5);
            motorControl(MOTOR_FORWARD, 120);
            avDelayTimeCount = 70; //延时 1050ms
            return;
        }
    }

    //右转
    if (avLeftAvgDistance > avRightAvgDistance)
    {
        if (avIsFirstDelay)
        {
            avIsFirstDelay = false;
            dirServoControl(TURN_RIGHT, TURN_MAX_DEG - 5);
            motorControl(MOTOR_BACK, 200);
            avDelayTimeCount = 30; //延时
            return;
        }

        if (avIsSencondDelay)
        {
            avIsSencondDelay = false;
            dirServoControl(TURN_LEFT, TURN_MAX_DEG - 5);
            motorControl(MOTOR_FORWARD, 120);
            avDelayTimeCount = 70; //延时
            return;
        }
    }
}
//--------------------------自动寻迹 结束-------------------------


//---------------------------------指令解析 开始------------------------------
//读取控制指令
void readCommandData()
{
    if (Serial.available() > 0)
    {
        int count = 0;
        String comdata = "";
        delay(2); //延时, 待接收完数据
        while (Serial.available() > 0)
        {
            count++;
            byte d = Serial.read();
            if (d == 0)
            {
                continue;
            }
            comdata += char(d);
            if (count % 16 == 0)
            {
                delay(2); //延时, 待接收完数据
            }
            if (count > 64)
            {
                Serial.flush();
                break;
            }
        }

        if (comdata.length() > 0)
        {
            resolveCommandData(comdata);
        }
    }
}

//解析指令
//指令格式 "[指令名:指令参数1&指令参数2&指令参数3]"
void resolveCommandData(String comdata)
{
    byte si = comdata.indexOf('['); //指令开始字符
    byte ei = comdata.indexOf(']'); //指令结束字符

    if (!(si > -1 && ei > -1 && ei > si))
    {
        return;
    }

    comdata = comdata.substring(si + 1, ei);
    byte commandIndex = comdata.indexOf(':');
    if (commandIndex <= 1)
    {
        return;
    }

    //指令名
    String commandName = comdata.substring(0, commandIndex);
    //指令内容
    String commandBody = comdata.substring(commandIndex + 1);

    if (commandName.equals("mc"))
    {
        resolveManualControl(commandBody);
        return;
    }
    if (commandName.equals("cm"))
    {
        resolveSetCarMode(commandBody);
        return;
    }
    if (commandName.equals("tms"))
    {
        resolveSetTraceMaxSpeed(commandBody);
        return;
    }
    if (commandName.equals("tmd"))
    {
        resolveSetTraceMaxDeg(commandBody);
        return;
    }
    if (commandName.equals("r-p"))
    {
        resolveReadCarParams();
        return;
    }
}

//手动遥控控制指令
//指令例子: [mc:200&20] , [mc:-150&-30]
//前进+,后退-
//右转+,左转-
void resolveManualControl(String commandBody)
{
    if (carMode != CARMODE_MANUAL_CONTROL)
    {
        return;
    }
    String ret = "[mc:";
    ret += commandBody;
    ret += "]";
    Serial.print(ret);

    byte paramIndex = commandBody.indexOf('&');
    if (paramIndex == -1)
    {
        return;
    }
    String speedStr = commandBody.substring(0, paramIndex);
    if (speedStr.length() == 0)
    {
        return;
    }
    String turnDegStr = commandBody.substring(paramIndex + 1);
    if (turnDegStr.length() == 0)
    {
        return;
    }

    mcSpeedDir = MOTOR_FORWARD; //速度的正负
    mcTurnDir = TURN_RIGHT;     //转弯角度的正负
    if (speedStr.charAt(0) == '-')
    {
        speedStr = speedStr.substring(1);
        mcSpeedDir = MOTOR_BACK;
    }
    if (turnDegStr.charAt(0) == '-')
    {
        turnDegStr = turnDegStr.substring(1);
        mcTurnDir = TURN_LEFT;
    }
    mcMotorSpeed = speedStr.toInt();
    mcTurnDeg = turnDegStr.toInt();
    mcLastReceiveTime = millis(); //记录接收指令的时间
}

//设置小车模式
//[cm:1],[cm:2]
void resolveSetCarMode(String commandBody)
{
    byte mode = commandBody.toInt();
    if (mode == CARMODE_AUTO_TRACE || mode == CARMODE_MANUAL_CONTROL || mode == CARMODE_AUTO_AVOIDING)
    {
        carMode = mode;
        String ret = "[cm:";
        ret += mode;
        ret += "]";
        Serial.print(ret);
    }
}

//设置小车寻迹 最大速度
void resolveSetTraceMaxSpeed(String commandBody)
{
    atMaxSpeed = commandBody.toInt();
    if (atMaxSpeed < 40)
    {
        atMaxSpeed = 40;
    }
    String ret = "[tms:";
    ret += atMaxSpeed;
    ret += "]";
    Serial.print(ret);
}

//设置小车寻迹最大转弯角度
void resolveSetTraceMaxDeg(String commandBody)
{
    atTurnMaxDeg = commandBody.toInt();
    if (atTurnMaxDeg > TURN_MAX_DEG)
    {
        atTurnMaxDeg = TURN_MAX_DEG;
    }
    String ret = "[tmd:";
    ret += atTurnMaxDeg;
    ret += "]";
    Serial.print(ret);
}
//同步小车参数
void resolveReadCarParams()
{
    String ret = "[r-p:";
    ret += "cm=";
    ret += carMode;
    ret += "&atTMD=";
    ret += atTurnMaxDeg;
    ret += "&atMS=";
    ret += atMaxSpeed;
    ret += "]";
    Serial.print(ret);
}
//---------------------------------指令解析 结束------------------------------

//初始化手动控制常量
void initManualControlParam()
{
    mcMotorSpeed = 0;
    mcTurnDeg = 0;
    mcSpeedDir = MOTOR_STOP;
    mcTurnDir = TURN_MIDDLE;
}

//arduino 初始设置
void setup()
{
    Serial.begin(115200);

    //驱动电机
    pinMode(motor_pin_1, OUTPUT);
    pinMode(motor_pin_2, OUTPUT);

    //转向舵机 中间位置
    dirServo.attach(dir_servo_pin);
    dirServo.write(90);

    //红外寻迹检测
    pinMode(infrared1_pin, INPUT);
    pinMode(infrared2_pin, INPUT);
    pinMode(infrared3_pin, INPUT);

    //超声波测距
    pinMode(avoiding_trig_pin, OUTPUT);
    pinMode(avoiding_echo_pin, INPUT);
    //测距云台舵机
    avoidingServo.attach(avoiding_servo_pin);
    avoidingServo.write(avServoMiddleDeg);

    delay(3000);

    //设置中断，每15ms进入一次中断服务程序 secondThread()
    MsTimer2::set(15, secondThread);
    MsTimer2::start(); //开始计时

    initManualControlParam();
}

//arduino 主线程
void loop()
{
    readCommandData();
    if (carMode == CARMODE_AUTO_TRACE)
    {
        autoTraceControl();
    }
    if (carMode == CARMODE_MANUAL_CONTROL)
    {
        motorControl(mcSpeedDir, mcMotorSpeed);
        dirServoControl(mcTurnDir, mcTurnDeg);
    }
    if (carMode == CARMODE_AUTO_AVOIDING)
    {
        loopDistanceMeasure2();
    }
}

//中断服务程序, 模拟多线程，用作副线程
void secondThread()
{
    timeCount++;
    if (timeCount % 3 == 0)
    {
        //读取串口数据, 数据量不能太大
        readCommandData();
    }
    if (carMode == CARMODE_AUTO_TRACE)
    {
        byte infrared1 = digitalRead(infrared1_pin);
        byte infrared2 = digitalRead(infrared2_pin);
        byte infrared3 = digitalRead(infrared3_pin);

        atLastInfraredStatus = infrared1 * 4 + infrared2 * 2 + infrared3 * 1;
    }
    if (carMode == CARMODE_MANUAL_CONTROL)
    {
        //1s 未接收到控制信号,小车停止
        if (millis() - mcLastReceiveTime > 1000)
        {
            initManualControlParam();
        }
    }
    if (carMode == CARMODE_AUTO_AVOIDING)
    {
        //autoAvoidingControl2();
    }
}
