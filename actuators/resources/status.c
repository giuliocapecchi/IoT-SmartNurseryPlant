#include "contiki.h"
#include "coap-engine.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

static void res_get_handler(coap_message_t *request,coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_hello,
         "title=\"Hello world: ?len=0..\";rt=\"Text\"",
         res_get_handler, // GET Handler
         NULL,            // POST Handler
         NULL,            // PUT Handler
         NULL);           // DELETE Handler


static int i = 1;

static void res_get_handler(coap_message_t *request,coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset){
    const char *len = NULL;
    char const *const message = "Hello World! ABCDEFGHIJKLMNOPQRSTUVWXYZ\n";
    int length = 12; // it's the lenght of the 'Hello World!' string without the alphabet
	printf("get_handler chiamato:%d volte\n",i);
    i++;
    if(coap_get_query_variable(request,"len", &len)){
        length = atoi(len);
        if(length<0){
            length = 0;
        }
        if(length > REST_MAX_CHUNK_SIZE){
            length = REST_MAX_CHUNK_SIZE;
        }
        memcpy(buffer,message,length);
    }else{
        memcpy(buffer,message,length);
    }


    coap_set_header_content_format(response, TEXT_PLAIN); /* text_plain is the default, could be omitted */
    coap_set_header_etag(response, (uint8_t *)&length, 1);
    coap_set_payload(response,buffer,length);
}