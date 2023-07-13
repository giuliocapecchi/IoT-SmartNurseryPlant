#include "contiki.h"
#include "coap-engine.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include "os/dev/leds.h"



static void res_get_handler(coap_message_t *request,coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_put_handler(coap_message_t *request,coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

extern int get_state();
extern bool get_active();
extern void set_active(bool value);
extern void set_state(int value);
extern void sleep();

RESOURCE(res_actuator,
         "title=\"Actuator Resource: ?len=0..\";rt=\"Text\"",
         res_get_handler, // GET Handler
         NULL,            // POST Handler
         res_put_handler, // PUT Handler
         NULL);           // DELETE Handler


static void res_get_handler(coap_message_t *request,coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
   
    int state = get_state();
    printf("get_handler chiamato, state = %d\n", state);
    sprintf((char*)buffer, "{\"value\":%d}", state);

    int length = strlen((char*)buffer);

    coap_set_header_content_format(response, APPLICATION_JSON); /* text_plain is the default, could be omitted */
    coap_set_header_etag(response, (uint8_t *)&length, 1);
    coap_set_payload(response,buffer,length);
}

static void res_put_handler(coap_message_t *request,coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
    const char *parameter = NULL;
  if(coap_get_post_variable(request, "status", &parameter)){
    if(strcmp(parameter,"off")==0 && get_active()==true ){
        leds_off(4);
        leds_off(8);
        leds_off(2);

        leds_on(1);
        set_active(false);
    }else if(strcmp(parameter,"on")==0 && get_active()==false ){
        leds_off(1);
        set_active(true);
    }else if(strcmp(parameter,"1")==0){ // force state to actuator
        set_active(true);
        set_state(atoi(parameter));
        sleep(); // function defined in the actuator, to actully show that a state was forced externally
    }else if(strcmp(parameter,"2")==0){
        set_active(true);
        set_state(atoi(parameter));
        sleep();
    }else if(strcmp(parameter,"3")==0){
        set_active(true);
        set_state(atoi(parameter));
        sleep();
    }

    coap_set_status_code(response, CHANGED_2_04);
  }else{
    coap_set_status_code(response, BAD_REQUEST_4_00);
  }
}