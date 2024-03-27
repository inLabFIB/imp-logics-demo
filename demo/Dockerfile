FROM jupyter/minimal-notebook:ubuntu-22.04

LABEL authors="inlab@fib.upc.edu"

USER root

# Install dependencies
RUN apt-get update \
    && apt-get install -y \
              software-properties-common \
              curl \
              openjdk-17-jdk \
    && apt-get clean

# Unpack and install the kernel
RUN curl -L https://github.com/SpencerPark/IJava/releases/download/v1.3.0/ijava-1.3.0.zip > ijava-kernel.zip
RUN unzip ijava-kernel.zip -d ijava-kernel \
  && cd ijava-kernel \
  && python3 install.py --sys-prefix

## Cleanup
RUN rm ijava-kernel.zip
#
## Set user back to priviledged user.
USER $NB_USER