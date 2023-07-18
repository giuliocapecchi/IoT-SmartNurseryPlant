#include "contiki.h"
#include "net/routing/routing.h"
#include "mqtt.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "lib/sensors.h"
#include "dev/button-hal.h"
#include "dev/leds.h"
#include "os/sys/log.h"
#include "mqtt-client.h"
#include "os/dev/leds.h"

#include <string.h>
#include <stdlib.h>
#include <strings.h>
/*---------------------------------------------------------------------------*/
#define LOG_MODULE "mqtt-client"
#ifdef MQTT_CLIENT_CONF_LOG_LEVEL
#define LOG_LEVEL MQTT_CLIENT_CONF_LOG_LEVEL
#else
#define LOG_LEVEL LOG_LEVEL_DBG
#endif

/*---------------------------------------------------------------------------*/
/* MQTT broker address. */
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"

static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

// Defaukt config values
#define DEFAULT_BROKER_PORT         1883
#define DEFAULT_PUBLISH_INTERVAL    (10 * CLOCK_SECOND)


// We assume that the broker does not require authentication


/*---------------------------------------------------------------------------*/
/* Various states */
static uint8_t state;

#define STATE_INIT    		  0
#define STATE_NET_OK    	  1
#define STATE_CONNECTING      2
#define STATE_CONNECTED       3
#define STATE_SUBSCRIBED      4
#define STATE_DISCONNECTED    5



/*---------------------------------------------------------------------------*/
/* Maximum TCP segment size for outgoing segments of our socket */
#define MAX_TCP_SEGMENT_SIZE    32
#define CONFIG_IP_ADDR_STR_LEN   64
/*---------------------------------------------------------------------------*/
/*
 * Buffers for Client ID and Topics.
 * Make sure they are large enough to hold the entire respective string
 */
#define BUFFER_SIZE 64

static char client_id[BUFFER_SIZE];
static char pub_topic[BUFFER_SIZE];
static char sub_topic[BUFFER_SIZE];


// Periodic timer to check the state of the MQTT client
#define STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
static struct etimer periodic_timer;

// definition of utility variables 
static struct ctimer humidity_timer;
static bool first_sample=true;


/*---------------------------------------------------------------------------*/
/*
 * The main MQTT buffers.
 * We will need to increase if we start publishing more data.
 */
#define APP_BUFFER_SIZE 512
static char app_buffer[APP_BUFFER_SIZE];
/*---------------------------------------------------------------------------*/
static struct mqtt_message *msg_ptr = 0;

static struct mqtt_connection conn;

mqtt_status_t status;
char broker_address[CONFIG_IP_ADDR_STR_LEN];

/*------------------------------------------------------*/
// SIMULATION OF HUMIDITY 

// random starting humidity value between 0 and 4 degrees
static uint8_t actual_humidity;
// optimal relative humidity value between 35% and 60%
static uint8_t lower_bound_humidity = 35;
static uint8_t upper_bound_humidity = 60;
static int increment;

static void humidity_sampling(){
    // function is automatically called every 10 seconds
     printf("Publishing to topic %s\n",sub_topic);

    if(first_sample==true)
        first_sample=false;

    actual_humidity += increment;

    if( (actual_humidity > upper_bound_humidity && increment > 0 ) || 
        (actual_humidity < lower_bound_humidity && increment < 0)){
            increment *=-1;
         }

	sprintf(pub_topic, "%s", "humidity");
	sprintf(app_buffer, "{ \"value\": %d }", actual_humidity);
				
	mqtt_publish(&conn, NULL, pub_topic, (uint8_t *)app_buffer,
            strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);

    ctimer_set(&humidity_timer, DEFAULT_PUBLISH_INTERVAL, humidity_sampling, NULL);
}

/*---------------------------------------------------------------------------*/
PROCESS(mqtt_client_process, "Humidity sensor MQTT");
AUTOSTART_PROCESSES(&mqtt_client_process);


/*---------------------------------------------------------------------------*/
static void
pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk,
            uint16_t chunk_len)
{

    LOG_INFO("Humidity publish handler called: Received on topic = %s, chunk = %s\n", topic, chunk);
}
/*---------------------------------------------------------------------------*/
static void
mqtt_event(struct mqtt_connection *m, mqtt_event_t event, void *data)
{
  switch(event) {
  case MQTT_EVENT_CONNECTED: {
    printf("Application has a MQTT connection\n");

    state = STATE_CONNECTED;
    break;
  }
  case MQTT_EVENT_DISCONNECTED: {
    printf("MQTT Disconnect. Reason %u\n", *((mqtt_event_t *)data));

    state = STATE_DISCONNECTED;
    process_poll(&mqtt_client_process);
    break;
  }
  case MQTT_EVENT_PUBLISH: {
    msg_ptr = data;

    pub_handler(msg_ptr->topic, strlen(msg_ptr->topic),
                msg_ptr->payload_chunk, msg_ptr->payload_length);
    break;
  }
  case MQTT_EVENT_SUBACK: {
#if MQTT_311
    mqtt_suback_event_t *suback_event = (mqtt_suback_event_t *)data;

    if(suback_event->success) {
      printf("Application is subscribed to topic successfully\n");
    } else {
      printf("Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
    }
#else
    printf("Application is subscribed to topic successfully\n");
#endif
    break;
  }
  case MQTT_EVENT_UNSUBACK: {
    printf("Application is unsubscribed to topic successfully\n");
    break;
  }
  case MQTT_EVENT_PUBACK: {
    printf("Publishing complete.\n");
    break;
  }
  default:
    printf("Application got a unhandled MQTT event: %i\n", event);
    break;
  }
}

static bool
have_connectivity(void)
{
  if(uip_ds6_get_global(ADDR_PREFERRED) == NULL ||
     uip_ds6_defrt_choose() == NULL) {
    return false;
  }
  return true;
}

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(mqtt_client_process, ev, data)
{

  PROCESS_BEGIN();

  printf("Humidity MQTT Client\n");

  // randomic choice of starting humidity and increment
  actual_humidity = (rand() % 91) + 5;  //need to avoid a negative humidity (measured in percentage) 
                                        //and a value higher than 100 also after first update (+ or - 5). 
                                        //We start from a value between 5 and 95
  increment = 5 * ((rand() % 2  == 0) ? -1 : 1);

  // Initialize the ClientID as MAC address
  snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
                     linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
                     linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
                     linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

  // Broker registration					 
  mqtt_register(&conn,&mqtt_client_process, client_id, mqtt_event,
                  MAX_TCP_SEGMENT_SIZE);
				  
  state=STATE_INIT;
				    
  // Initialize periodic timer to check the status 
  etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);
  

  /* Main loop */
  while(1) {

    PROCESS_YIELD();

    if((ev == PROCESS_EVENT_TIMER && data == &periodic_timer) || 
	      ev == PROCESS_EVENT_POLL){
			  			  
		  if(state==STATE_INIT){
        leds_on(LEDS_RED);
			 if(have_connectivity()==true)  
				 state = STATE_NET_OK;
		  } 
		  
		  if(state == STATE_NET_OK){
			  // Connect to MQTT server
			  printf("Connecting!\n");
			  
			  memcpy(broker_address, broker_ip, strlen(broker_ip));
			  
			  mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT,
						   (DEFAULT_PUBLISH_INTERVAL * 3) / CLOCK_SECOND,
						   MQTT_CLEAN_SESSION_ON);
			  state = STATE_CONNECTING;
		  }
		  
		  if(state==STATE_CONNECTED){
		  
			  // Subscribe to a topic
			  strcpy(sub_topic,"humidity");  

			  status = mqtt_subscribe(&conn, NULL, sub_topic, MQTT_QOS_LEVEL_0);

			  printf("Subscribing to topic %s!\n",sub_topic);
			  if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
                LOG_ERR("Tried to subscribe but command queue was full!\n");
                PROCESS_EXIT();
			  }
			  
			  state = STATE_SUBSCRIBED;
		  }

			  
		if(state == STATE_SUBSCRIBED){  
			leds_off(LEDS_RED);
      leds_on(LEDS_GREEN);
      //publish
      if(first_sample==1)
        humidity_sampling();
               
              
		
		} else if ( state == STATE_DISCONNECTED ){
		   LOG_ERR("Disconnected form MQTT broker\n");	
		   // Recover from error
           state=STATE_INIT;
           leds_off(LEDS_GREEN);
           leds_on(LEDS_RED);
		}
		
		etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);
      
    }else if(ev == button_hal_press_event){
      etimer_stop(&periodic_timer);
      ctimer_stop(&humidity_timer);
      printf("button was pressed! OFF state\n");
      leds_off(LEDS_GREEN);
      leds_on(LEDS_RED);
      PROCESS_WAIT_EVENT_UNTIL( ev == button_hal_press_event);
      printf("ON state\n");
      leds_on(LEDS_GREEN);
      leds_off(LEDS_RED);
      etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);
      ctimer_set(&humidity_timer, DEFAULT_PUBLISH_INTERVAL, humidity_sampling, NULL);
    }

  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
