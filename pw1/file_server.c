#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#define BUF_SIZE 4096

int main() {
    int ss, cli, pid;
    struct sockaddr_in ad;
    char s[100];
    socklen_t ad_length = sizeof(ad);

    // create the socket
    ss = socket(AF_INET, SOCK_STREAM, 0);

    // bind the socket to port 12345
    memset(&ad, 0, sizeof(ad));
    ad.sin_family = AF_INET;
    ad.sin_addr.s_addr = INADDR_ANY;
    ad.sin_port = htons(12345);
    bind(ss, (struct sockaddr *)&ad, ad_length);

    // then listen
    listen(ss, 0);

    while (1) {
        // an incoming connection
        cli = accept(ss, (struct sockaddr *)&ad, &ad_length);

        pid = fork();
        if (pid == 0) {
            // I'm the son, I'll serve this client
            printf("client connected\n");

            char file_ext[10];
            read(cli, file_ext, 10);

            char name[15];
            strcpy(name, "received.");
            char* file_name = strcat(name, file_ext);

            int fd = open(file_name, O_CREAT | O_TRUNC | O_WRONLY);

            char buffer[BUF_SIZE];

            if (fd < 0) exit(1);
            while (1) {
                printf("%s", buffer);
                int n = read(cli, buffer, BUF_SIZE);
                if (n == 0) break;
                write(fd, buffer, n);
            }

            close(cli);
            close(fd);

            return 0;
        }
        else {
            // I'm the father, continue the loop to accept more clients
            continue;
        }
    }
    // disconnect
    close(cli);

}
