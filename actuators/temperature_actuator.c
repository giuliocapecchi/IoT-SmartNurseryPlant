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
char *service_url = "/registration";

extern coap_resource_t res_actuator;

static struct etimer et;
static struct etimer et2;
static struct etimer et3;

// state == 2 : non dangerous values received
// state == 1 || 3 : danger zone, actuator needs to be turned on 
// state == 0 : device is off
static int state = 2 ;
static bool controlled = false;
static bool registered = false;
static bool off = false;


int get_state() {
    return state;
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

void set_state(int value){
    printf("sono nella set state\n");
    etimer_reset(&et3);
    if(!controlled && !off){   
        printf("sono nell if della set state\n");
        state = value;
        leds_management();
    }
}

void sleep(){ //used in the resource when forcing a state
    leds_management();
    leds_on(1);
    printf("sono nella sleep!\n");
    controlled = true;   
    etimer_set(&et, 5*CLOCK_SECOND);
//////
    
}

void client_response_handler(coap_message_t *response) {
    const uint8_t *chunk;
    if (response==NULL) {
        printf("Request timed out, server is unrecheable\n");
        // server is unreacheable, go back in off state.
        return;
    }
    registered=true;
    leds_management();
    printf("Registered!\n");
    etimer_set(&et3, 30*CLOCK_SECOND );
    int len=coap_get_payload(response, &chunk);
    printf("%.*s", len, (char *)chunk);
    leds_off(1);
    
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


    // Populate the coap_endpoint_t data structure
    coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);  //Nel while?
    leds_on(1);
    btn = button_hal_get_by_index(0);

    
    coap_activate_resource(&res_actuator, "temperature_actuator");

    while (1){

        while(!registered){
        coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
        coap_set_header_uri_path(request, service_url);
        char msg[50];
        sprintf(msg, "{\"topic\":\"temperature\", \"value\":%d}", state);
                    
        printf("msg : %s\n",msg);
        coap_set_payload(request, (uint8_t *)msg, strlen(msg));
        COAP_BLOCKING_REQUEST(&server_ep, request, client_response_handler);
        }


      //  PROCESS_WAIT_EVENT_UNTIL( (ev == PROCESS_EVENT_TIMER) || (ev == button_hal_press_event ) || (ev == button_hal_periodic_event));
        PROCESS_WAIT_EVENT();
        printf("dopo process yield\n");

        if(etimer_expired(&et3)){
            // no get requests from server for more than 2 minutes
            printf("Server disconnected! Registering again...\n");
            registered = false;
        }

        if(ev==button_hal_press_event && off == false){
            
            controlled = true;
            state = (state+1)%4;
            if(state==0)
                state++;
            printf("button pressed! New state:%d\n",state);
            leds_management();
            leds_on(1);                         //forced state, yellow led on
            etimer_set(&et2,7*CLOCK_SECOND);
            PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&et2) || ev == button_hal_press_event);
            leds_management();
            // stop et2 in case the button was pressed.
            etimer_stop(&et2);
            controlled = false;
            
        }

        if(ev == button_hal_periodic_event){
            btn = (button_hal_button_t *)data;
            if(btn->press_duration_seconds > 5) {
                printf("long button pressed!\n");
                off = (!off);
                if(off){
                   state = 0;
                    printf("off = true\n");
                }else{
                    state = 2;
                    printf("off = false\n");
                } 
                leds_management();
                btn->press_duration_seconds = 0;
            } 
        }
        
         if(etimer_expired(&et)){
            // 
            printf("Test!!!!\n");
        } 

        if(controlled == true){
            printf("sono in attesa...\n");
            etimer_set(&et,5*CLOCK_SECOND);
            PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&et));
            printf("fine attesa...\n");
            controlled = false;
        }
        
    }
    PROCESS_END();
}
