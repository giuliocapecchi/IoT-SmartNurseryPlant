#include "contiki.h"
#include "coap-engine.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

static void res_get_handler(coap_message_t *request,coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_put_handler(coap_message_t *request,coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

extern int state;

RESOURCE(res_actuator,
         "title=\"Actuator Resource: ?len=0..\";rt=\"Text\"",
         res_get_handler, // GET Handler
         NULL,            // POST Handler
         NULL, // PUT Handler
         NULL);           // DELETE Handler


static void res_get_handler(coap_message_t *request,coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
   
	printf("get_handler chiamato\n");
    int lenght = sizeof(state);
    memcpy(buffer,state,sizeof(state));  

    coap_set_header_content_format(response, TEXT_PLAIN); /* text_plain is the default, could be omitted */
    coap_set_header_etag(response, (uint8_t *)&length, 1);
    coap_set_payload(response,buffer,length);
}