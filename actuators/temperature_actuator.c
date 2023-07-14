#include "contiki.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki-net.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "os/dev/button-hal.h"
#include "etimer.h"
#include "JSON_utility.h"
#include "os/dev/leds.h"
#include "sys/log.h"

/* Log configuration */
#include "coap-log.h"

#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

// server IP and resource path
#define SERVER_EP "coap://[fd00::1]:5683"
char *service_url = "/temperature";

extern coap_resource_t res_actuator;

static struct etimer et;
static struct etimer et2;
// state == 2 : non dangerous values received
// state == 1 || 3 : danger zone, actuator needs to be turned on 
// state == 0 : device is off
static int state = 2 ;
static bool controlled = false;


int get_state() {
    return state;
}

void set_state(int value){
    state = value;
}

void leds_management(){
    if(state == 0){
        leds_off(2);
        leds_off(4);
        leds_off(8);
        leds_on(1);
    }
    else if(state == 1){
        leds_off(1);
        leds_off(2);
        leds_off(4);
        leds_on(8);
    } 
    else if(state == 2){
        leds_off(1);
        leds_off(2);
        leds_off(8);
        leds_on(4);
    }
    else if(state == 3){
        leds_off(1);
        leds_off(8);
        leds_off(4);
        leds_on(2);
    }
}

void sleep(){ //used in the resource when forcing a state

    leds_off(4);
    leds_off(8);
    leds_off(2);
    
    leds_on(1);
    if(state==1){
        leds_on(8); //blue
    }else if(state==2){
        leds_on(4); //green
    }else if(state==3){
        leds_on(2); //red
    }
    
    controlled = true;    
}

static int disconnected = 0;

void client_response_handler(coap_message_t *response) {
    const uint8_t *chunk;
    if (response==NULL) {
        puts("Request timed out");
        printf("Request timed out\n");
        // server is unreacheable, go back in off state.
        state = 0;
        disconnected = 1;
        leds_off(2);
        leds_off(4);
        leds_off(8);
        leds_on(1);
        return;
    }
    coap_get_payload(response, &chunk);
    //printf("|%.*s", len, (char *)chunk);
    leds_off(1);
    

    int value = extractValueFromJSON((char *)chunk);
    printf("Temperature_value: %d\n", value);

    if (value > 28){
        // Actuator needs to be activated!
        state = 3;
        leds_management();
    }else if(value < 18 ){
        // Actuator needs to be activated!
        state = 1;
        leds_management();
    }else{
        state = 2;
        leds_management();
    }


}


/* Declare and auto-start this file's process */
PROCESS(temperature_actuator, "CoAP Temperature Actuator");
AUTOSTART_PROCESSES(&temperature_actuator);

// The client includes two data structures
// coap_endpoint_t -> represents an endpoint
// coap_message_t -> represent the message
PROCESS_THREAD(temperature_actuator, ev, data){
    
    button_hal_button_t *btn;
    static coap_endpoint_t server_ep;
    static coap_message_t request[1]; /* This way the packet can be treated as pointer as usual. */
   
    PROCESS_BEGIN();
    coap_activate_resource(&res_actuator, "temperature_actuator");
    // Populate the coap_endpoint_t data structure
    coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
    
    etimer_set(&et,10*CLOCK_SECOND);
    leds_on(1);

    while (1){
        
        //leds_on(LEDS_RED); non funziona
        //leds_on(LEDS_GREEN); funziona
       
       // 0 non accende niente
       // 1 accende giallo SOPRA
       // 2 accende rosso sotto
       // 3 accende 1+2 insieme
       // 4 accende verde sotto
       // 5 accende 1 + 4
       // 6 giallo sotto (verde + rosso)
       // 7 sopra giallo sotto verde+rosso
       // 8 blu sotto
       // 9 blu sotto giallo sopra
       
       PROCESS_YIELD();

        if(ev==button_hal_press_event){
            state = (state+1)%4;
            leds_management();
            leds_on(1);                         //forced state, yellow led on
            etimer_set(&et2,15*CLOCK_SECOND);
            PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&et2));
        }

        if(ev== button_hal_periodic_event){
            btn = (button_hal_button_t *)data;
            if(btn->press_duration_seconds > 5) {
                state = 0;
                leds_management();
            } 
        }

        if(state==0){ 
            etimer_reset(&et);
            continue;
        }

        if(controlled){
            etimer_set(&et2,15*CLOCK_SECOND);
            PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&et2));
            controlled = false;
        }

        if(disconnected==1){
            // Populate the coap_endpoint_t data structure
            coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
            disconnected=0;
        }
        
        if(etimer_expired(&et)){
            
            // Prepare the message
            coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
            coap_set_header_uri_path(request, service_url);
            // Issue the request in a blocking manner
            // The client will wait for the server to reply (or the transmission to timeout)
            // dopo sta richiesta si esegue l'handler poi si torna qui
            COAP_BLOCKING_REQUEST(&server_ep, request, client_response_handler);
            etimer_reset(&et);
       	}
    }
    PROCESS_END();
}
