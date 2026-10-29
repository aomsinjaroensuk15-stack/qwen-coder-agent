cmake_minimum_required(VERSION 3.22.1)
project(llama-bridge CXX)

# llama.cpp ต้องเป็น submodule ที่ cpp/llama.cpp
add_subdirectory(llama.cpp)

add_library(llama-bridge SHARED llama_bridge.cpp)
target_link_libraries(llama-bridge llama)
