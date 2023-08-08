#include "contiki.h"
#include "coap-engine.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include "os/dev/leds.h"
#include "JSON_utility.h"


static void res_get_handler(coap_message_t *request,coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_put_handler(coap_message_t *request,coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

extern int get_state();
extern void set_state(int value);
extern void sleep();


RESOURCE(res_actuator,
         "title=\"Actuator Resource: Control System Actuator\";rt=\"Text",
         res_get_handler, // GET Handler
         NULL,            // POST Handler
         res_put_handler, // PUT Handler
         NULL);           // DELETE Handler


static void res_get_handler(coap_message_t *request,coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
   
    int state = get_state();
    printf("get_handler chiamato, state = %d\n", state);
    sprintf((char*)buffer, "{\"value\":%d}", state);

    int length = strlen((char*)buffer);

    coap_set_header_content_format(response, APPLICATION_JSON);
    coap_set_header_etag(response, (uint8_t *)&length, 1);
    coap_set_payload(response,buffer,length);
}

static void res_put_handler(coap_message_t *request,coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
     
  const uint8_t *payload = NULL;
  int payload_len = coap_get_payload(request, &payload);
  const char* json = (const char*)payload;

  if (payload_len > 0 && payload != NULL) {

    int value = extractValueFromJSON(json, "value");

    int forced = extractValueFromJSON(json, "forced");

    printf("Extracted values: value->%d , forced->%d\n",value,forced);

    coap_set_status_code(response, CHANGED_2_04);

    if(value==0 && get_state()!=0 ){ // turn OFF the actuator
        leds_off(4);
        leds_off(8);
        leds_off(2);
        leds_on(1);
        set_state(value);
    }else if(value==1){ // force state to actuator
        set_state(value);
        if(forced == 1)
          sleep(); // function defined in the actuator, to actully show that a state was forced externally
    }else if(value==2){
        set_state(value);
        if(forced == 1)
          sleep();
    }else if(value==3){
        set_state(value);
        if(forced == 1)
          sleep();
    }
  
  
  }else{
    coap_set_status_code(response, BAD_REQUEST_4_00);
  }
  
}