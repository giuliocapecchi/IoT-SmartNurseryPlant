#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Find substring inside of a given string
char* findSubstring(const char* str, const char* substr) {
    char* pos = strstr(str, substr);
    if (pos != NULL) {
        return pos + strlen(substr);
    }
    return NULL;
}

// Extract Value from JSON
int extractValueFromJSON(const char* json, const char* key) {
    char buf[100];
    strcpy(buf, "\"");
    strcat(buf,key);
    strcat(buf,"\":");
    const char* valueStart = findSubstring(json,buf);
    if (valueStart != NULL) {
        return atoi(valueStart);
    }else{
        char buf[100];
        strcpy(buf, key);
        strcat(buf,":");
        valueStart = findSubstring(json, buf);
        if (valueStart != NULL) 
            return atoi(valueStart);
    }
    return 0;  // Predefined value if "buf" string is not found
}