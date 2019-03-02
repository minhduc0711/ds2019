#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <fcntl.h>

#define BUF_SIZE 4096

char *get_filename_ext(const char *filename) {
    char *dot = strrchr(filename, '.');
    if (!dot) return "";
    else return dot + 1;
}

int main(int argc, char* argv[]) {
    int so;
    struct sockaddr_in ad;

    socklen_t ad_length = sizeof(ad);
    struct hostent *hep;

    // create socket
    int serv = socket(AF_INET, SOCK_STREAM, 0);

    // init address
    hep = gethostbyname(argv[1]);
    memset(&ad, 0, sizeof(ad));
    ad.sin_family = AF_INET;
    ad.sin_addr = *(struct in_addr *)hep->h_addr_list[0];
    ad.sin_port = htons(12345);

    // connect to server
    connect(serv, (struct sockaddr *)&ad, ad_length);

    char* file_name = argv[2];

    char buffer[BUF_SIZE];
    int fd = open(file_name, O_RDONLY);
    if (fd < 0) {
        exit(1);
    }

    char *file_ext = get_filename_ext(argv[2]);

    write(serv, file_ext, strlen(file_ext));

    while (1)
    {
        int n = read(fd, buffer, BUF_SIZE);
        printf("%s", buffer);
        if (n == 0)
            break;
        write(serv, buffer, n);
    }
    close(serv);
    close(fd);
}
