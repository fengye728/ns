// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently, but
// are changed infrequently
//

#pragma once

#define _WINSOCK_DEPRECATED_NO_WARNINGS
#define _CRT_SECURE_NO_WARNINGS

#include "targetver.h"

#include <WinSock2.h>
#include <iostream>
#include <windows.h>
#include <fstream>
#include <unordered_map>

#include "NxCoreAPI.h"
#include "NxCoreAPI_class.h"

#include "NxCoreOptionInfo.h"
#include "NxCoreOutputStream.h"
#include "NxCoreReader.h"

#pragma comment(lib, "ws2_32.lib")