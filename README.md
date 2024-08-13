## Spring Batch

****

Spring Batch 공부 내용을 정리했습니다.

**Spring Batch란**

spring Batch 는 대용량 데이터 처리를 위한 Spring Framework 의 서브 프로젝트로,배치 처리
작업을 효울적으로 실행하도록 도와줍니다. Spring Batch 를 사용하면 반복적이고
대량의 데이터를 처리하는 작업을 쉽게 설정하고 관리할 수 있습니다.

****
**Spring Batch의 주요 구성요소**

- Job : 배치 처리의 단위로, 여러 Step 으로 구성됩니다.
- Step : Job 의 세부 단위로, 각 Step 은 특정 작업(읽기, 처리, 쓰기)을 수행합니다.
- ItemReader : 데이터를 읽어오는 역할을 합니다.
- ItemProcessor : 데이터를 처리하는 역할을 합니다.
- ItemWriter : 데이터를 출력하는 역할을 합니다.
- JobRepository : Job 실행 정보를 저장하고 관리하는 역할을 합니다.
- JobLauncher : Job 을 실행하는 역할을 합니다.
-
**Spring Batch TransactionManager**

TransactionManager 는 Spring 에서 제공하는 인터페이스로, 데이터 베이스와 같은 외부 리소스를 사용할 때, 이를 관리하는 역할을 한다.
Spring Batch 에서는 Job 과 Step 에서 외부 리소스를 사용할때 TransactionManager 를 사용해 데이터의 정합성을 유지한다.

Spring Batch 에서는 Job 과 Step 에서 트랜잭션 처리를 위해 TransactionManager 를 사용합니다. 이를 위해서 Spring 에서 제공하는
PlatformTransactionManager  사용합니다.

Spring Batch 에서는 Job을 실행하기 전에 JobRepository 를 이용해 JobExecution 을 생성합니다. 이때 JobExecution 은
PlatformTransactionManager 를 이용해 트랜잭션 처리를 합니다.


INSERT INTO Sales (order_date, amount, order_no) VALUES ('2023-01-01', 1000, '1');
INSERT INTO Sales (order_date, amount, order_no) VALUES ('2023-01-02', 1500, '2');
INSERT INTO Sales (order_date, amount, order_no) VALUES ('2023-01-03', 2000, '3');
INSERT INTO Sales (order_date, amount, order_no) VALUES ('2023-01-04', 2500, '4');
INSERT INTO Sales (order_date, amount, order_no) VALUES ('2023-01-05', 3000, '5');
INSERT INTO Sales (order_date, amount, order_no) VALUES ('2023-01-06', 3500, '6');
INSERT INTO Sales (order_date, amount, order_no) VALUES ('2023-01-07', 4000, '7');
INSERT INTO Sales (order_date, amount, order_no) VALUES ('2023-01-08', 4500, '8');
INSERT INTO Sales (order_date, amount, order_no) VALUES ('2023-01-09', 5000, '9');
INSERT INTO Sales (order_date, amount, order_no) VALUES ('2023-01-10', 5500, '10');

-- SalesSum 데이터 삽입
INSERT INTO Sales_Sum (order_date, amount_sum) VALUES ('2023-01-01', 10000);
INSERT INTO Sales_Sum (order_date, amount_sum) VALUES ('2023-01-02', 15000);
INSERT INTO Sales_Sum (order_date, amount_sum) VALUES ('2023-01-03', 20000);
INSERT INTO Sales_Sum (order_date, amount_sum) VALUES ('2023-01-04', 25000);
INSERT INTO Sales_Sum (order_date, amount_sum) VALUES ('2023-01-05', 30000);
INSERT INTO Sales_Sum (order_date, amount_sum) VALUES ('2023-01-06', 35000);
INSERT INTO Sales_Sum (order_date, amount_sum) VALUES ('2023-01-07', 40000);
INSERT INTO Sales_Sum (order_date, amount_sum) VALUES ('2023-01-08', 45000);
INSERT INTO Sales_Sum (order_date, amount_sum) VALUES ('2023-01-09', 50000);
INSERT INTO Sales_Sum (order_date, amount_sum) VALUES ('2023-01-10', 55000);

