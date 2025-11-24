# stock

# 🌟 비관적 락 기반 동시성 제어 및 재고 관리 시스템


## 💡 프로젝트 개요


 이 프로젝트는 Spring Boot와 JPA를 사용하여 **데이터베이스의 비관적 락**을 활용한 고성능 동시성 제어 시스템을 구현하고 검증하는 것을 목표로 합니다.

 주문 발생 시 재고가 부족하면 자동으로 재고를 보충하는 비즈니스 로직을 포함하며, JMeter와 같은 부하 테스트 도구를 통해 락 메커니즘이 정상적으로 동작하여 재고 데이터의 무결성을 보장하는지 확인합니다.



## ⚙️ 기술 스택 및 의존성


#### 기술 스택

|  기술  |  버전  |  역할  |
|  :---  |  :---  |  :---  |
|  언어  |  Java 21+  |  핵심 비즈니스 로직 구현  |
|  프레임워크  |Spring Boot 3.x  |  애플리케이션 프레임워크  |
|  ORM  |  Spring Data JPA / HibernateDB  |  연동 및 ORM  |
|  데이터베이스  |  PostgreSQL  |  관계형 데이터베이스 (비관적 락 사용)  |
|  테스트  |  JUnit 5, Mockito  |  단위/통합 테스트  |
|  유틸리티  |  Lombok  |  보일러플레이트 코드 자동화  |

#### Dependencies

 
 **build.gradle**이 프로젝트에서 사용된 주요 Gradle 의존성 목록입니다.


 
## 📊 데이터베이스 스키마 모델


#### 1. Product 테이블

 상품 정보와 재고 현황을 관리합니다. 비관적 락이 이 테이블의 row에 적용됩니다.


|  필드명  |  타입  |  제약조건  |  설명  |
|  :---  |  :---  |  :---  |  :---  |
|  id  |  BIGINT  |  PK, Auto Increment  |  상품 고유 ID  |
|  name  |  VARCHAR  |  NOT NULL  |  상품 이름  |
|  stock  |  BIGINT  |  NOT NULL  |  현재 재고 수량  |
|  price  |  BIGINT  |  NOT NULL  |  개당 판매 가격  |
|  sold  |  BIGINT  |  NOT NULL, DEFAULT 0  |  누적 판매 수량(초기값 0L)  |


#### 2. Product_Order 테이블
  
 주문 내역을 기록합니다. 
 
 
|  필드명  |  타입  |  제약조건  |  설명  |
|  :---  |  :---  |  :---  |  :---  |
|  id  |  BIGINT  |  PK, Auto Increment  |  주문 고유 ID  |
|  productId  |  BIGINT  |  NOT NULL  |  주문된 상품 ID (Product 테이블 참조)  |
|  quantity  |  BIGINT  |  NOT NULL  |  주문 수량  |
|  pricePerUnit  |  BIGINT  |  NOT NULL  |  주문 시점의 상품 단가  |
|  totalRevenue  |  BIGINT  |  NOT NULL  |  총 주문 금액 (quantity * pricePerUnit)  |
|  orderDate  |  TIMESTAMP  |  NOT NULL  |  주문 발생 시간  |



## 🚀 확장성 및 대안 잠금 전략


 현재 프로젝트는 **비관적 락(DB Lock)** 을 사용하여 동시성 문제를 해결하고 있습니다. 이는 구현이 단순하고 데이터 무결성이 보장되지만, 높은 동시성 환경이나 MSA 환경에서는 DB Connection Pool 점유 및 대기 시간 증가로 인해 성능 저하를 유발할 수 있습니다.
 대안 (분산 락)대규모 동시성 처리를 위해서는 DB 락 대신 Redis 기반의 분산 락 도입이 필요합니다.  Redis는 외부 서버에서 Lock 키를 관리하므로, DB 커넥션 풀을 점유하지 않고 여러 서버 인스턴스 간의 공유 자원 접근을 안전하게 제어하여 처리량(Throughput)을 크게 향상시킬 수 있습니다.
 
 
 
## 🛠️ 환경 설정 및 실행 방법


#### 1. 📋 필수 설치 항목

|  도구  |  용도  |  다운로드 및 설치 가이드  |
|  :---  |  :---  |  :--- |
|  JDK (21+)  |  애플리케이션 실행 환경  |	 Oracle JDK / OpenJDK  |  
|  PostgreSQL  |  데이터베이스 서버  |  [PostgreSQL 다운로드](https://www.postgresql.org/download/)  |
|  Apache JMeter  |  동시성 및 부하 테스트 도구  |  [JMeter 다운로드](https://jmeter.apache.org/download_jmeter.cgi)  |
|  Redis  |  분산 락/캐싱 환경 구축 시 필요  |  [Redis 다운로드](https://redis.io/downloads/)  |



#### 2. 데이터베이스 설정


 DB 생성: stock_db라는 이름의 데이터베이스를 생성합니다.

 접속 정보 확인: application.yml에 명시된 기본 접속 정보 사용 


#### 3. 연결


 redis 설치 이후, wls에 sudo service redis-server start. (redis-cli ping으로 서버 연결 확인 가능)

 pgAdmin에서 데이터베이스 연결

 

## 📈 JMeter 동시성 테스트 가이드


#### 1. 테스트 API

|  기능  |  HTTP Method  |  URL  |  설명  |
|  :---  |  :---  |  :---  |  :---  |
|  상품 등록  |  POST  |  /api/product?stock=5&price={가격}  |  초기 재고 설정주문  |
|  요청  |  POST  |  /api/order/product?quantity=10  |  동시성 테스트 대상  |
|  결과 확인  |  GET  |  /api/result  |  최종 재고, 판매량, 총 수익 확인  |


#### 2. 예상 결과

 JMeter 테스트 (예: 100 스레드가 10개씩 주문) 완료 후, /api/result API 호출 결과는 다음과 같아야 합니다.
 
 총 주문 건수: 100건과 일치 
 
 최종 재고: 초기 재고와 동일하게 유지 (자동 입고 로직 증명)총 수익: 총 주문 수량 * 개당 가격과 일치


 **[테스트 영상](https://drive.google.com/file/d/1D-9OTB06Cu5tIjxCi9v0TwqMkYvp9Z3x/view?usp=sharing)**
