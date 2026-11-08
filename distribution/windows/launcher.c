#ifndef UNICODE
#define UNICODE
#endif
#ifndef _UNICODE
#define _UNICODE
#endif
#include <windows.h>
#include <shellapi.h>
#include <shlobj.h>
#include <wchar.h>
#include <stdlib.h>
#include <stdio.h>

/* Native x64 bootstrap. Java and its notices are bundled beside the launcher. */
static wchar_t command[32768];
static size_t used;
static void fail(const wchar_t *message) {
#ifdef SERVER
    fwprintf(stderr,L"%ls\n",message);
#else
    MessageBoxW(NULL,message,L"PokeWilds-F",MB_OK|MB_ICONERROR);
#endif
    ExitProcess(1);
}
static void put(wchar_t c) {
    if(used+2>=32768) fail(L"Command line is too long.");
    command[used++]=c; command[used]=0;
}
/* Windows argv quoting, including embedded quotes and trailing backslashes. */
static void argument(const wchar_t *s) {
    if(used) put(L' ');
    put(L'"');
    while(*s) {
        size_t slashes=0;
        while(*s==L'\\') { slashes++; s++; }
        size_t count=(*s==L'"' || !*s) ? slashes*2 : slashes;
        while(count--) put(L'\\');
        if(!*s) break;
        if(*s==L'"') put(L'\\');
        put(*s++);
    }
    put(L'"');
}
static int launch(int argc,wchar_t **argv) {
    wchar_t root[32768],java[32768],runtime[32768],classpath[32768],path[32768];
    DWORD length=GetModuleFileNameW(NULL,root,32768);
    if(!length || length>=32768) fail(L"Cannot determine launcher directory.");
    wchar_t *slash=wcsrchr(root,L'\\'); if(!slash) fail(L"Invalid launcher directory."); *slash=0;
    if(wcslen(root)>12000) fail(L"Please extract to a shorter directory path.");
    swprintf(java,32768,L"%ls\\jre\\bin\\java.exe",root);
    if(GetFileAttributesW(java)==INVALID_FILE_ATTRIBUTES) fail(L"Bundled Java is missing. Extract the entire release ZIP, including jre and lib.");
    DWORD env=GetEnvironmentVariableW(L"PW_RUNTIME",runtime,32768);
    if(env>=32768) fail(L"PW_RUNTIME is too long.");
    if(!env) swprintf(runtime,32768,L"%ls\\runtime",root);
    DWORD resolved=GetFullPathNameW(runtime,32768,path,NULL);
    if(!resolved || resolved>=32768) fail(L"Cannot resolve runtime directory.");
    wcscpy(runtime,path);
    int made=SHCreateDirectoryExW(NULL,runtime,NULL);
    if(made!=ERROR_SUCCESS && made!=ERROR_ALREADY_EXISTS && made!=ERROR_FILE_EXISTS) fail(L"Cannot create runtime directory. Extract the game into a writable folder.");
    swprintf(classpath,32768,L"%ls\\lib\\pokewilds-engine.jar;%ls\\lib\\runtime-deps.jar",root,root);
    argument(java);
#ifdef SERVER
    argument(L"-Xmx2g"); argument(L"-Djava.awt.headless=true");
#else
    wchar_t settings[32768];
    swprintf(path,32768,L"%ls\\config\\settings.txt",root);
    swprintf(settings,32768,L"%ls\\settings.txt",runtime);
    CopyFileW(path,settings,TRUE);
    swprintf(path,32768,L"%ls\\tmp",runtime);
    SHCreateDirectoryExW(NULL,path,NULL);
    wchar_t temp[32768]; swprintf(temp,32768,L"-Djava.io.tmpdir=%ls",path); argument(temp);
#endif
    argument(L"-cp"); argument(classpath);
#ifdef SERVER
    argument(L"com.pkmngen.game.DedicatedServer");
    argument(argc>1?argv[1]:L"shared"); argument(argc>2?argv[2]:L"54555");
#else
    argument(L"com.pkmngen.game.desktop.DesktopLauncher");
    for(int i=1;i<argc;i++) argument(argv[i]);
#endif
    STARTUPINFOW startup={0}; startup.cb=sizeof(startup);
    PROCESS_INFORMATION process={0}; DWORD flags=0;
#ifdef SERVER
    startup.dwFlags=STARTF_USESTDHANDLES;
    HANDLE standard[3]; DWORD slots[3]={STD_INPUT_HANDLE,STD_OUTPUT_HANDLE,STD_ERROR_HANDLE};
    for(int i=0;i<3;i++) {
        if(!DuplicateHandle(GetCurrentProcess(),GetStdHandle(slots[i]),GetCurrentProcess(),&standard[i],0,TRUE,DUPLICATE_SAME_ACCESS))
            fail(L"Cannot inherit console input/output handles.");
    }
    startup.hStdInput=standard[0]; startup.hStdOutput=standard[1]; startup.hStdError=standard[2];
#endif
#ifndef SERVER
    SECURITY_ATTRIBUTES inherit={sizeof(SECURITY_ATTRIBUTES),NULL,TRUE};
    swprintf(path,32768,L"%ls\\launcher.log",runtime);
    HANDLE log=CreateFileW(path,FILE_APPEND_DATA,FILE_SHARE_READ|FILE_SHARE_WRITE,&inherit,OPEN_ALWAYS,FILE_ATTRIBUTE_NORMAL,NULL);
    HANDLE input=CreateFileW(L"NUL",GENERIC_READ,FILE_SHARE_READ|FILE_SHARE_WRITE,&inherit,OPEN_EXISTING,0,NULL);
    if(log==INVALID_HANDLE_VALUE || input==INVALID_HANDLE_VALUE) fail(L"Cannot open launcher log in runtime directory.");
    startup.dwFlags=STARTF_USESTDHANDLES; startup.hStdOutput=log; startup.hStdError=log; startup.hStdInput=input;
    flags=CREATE_NO_WINDOW;
#endif
    BOOL started=CreateProcessW(java,command,NULL,NULL,TRUE,flags,NULL,runtime,&startup,&process);
#ifndef SERVER
    CloseHandle(log); CloseHandle(input);
#else
    for(int i=0;i<3;i++) CloseHandle(standard[i]);
#endif
    if(!started) fail(L"Could not start bundled Java. Extract the entire archive and try again.");
    WaitForSingleObject(process.hProcess,INFINITE);
    DWORD status=1; GetExitCodeProcess(process.hProcess,&status);
    CloseHandle(process.hThread); CloseHandle(process.hProcess);
    return (int)status;
}
#ifdef SERVER
int wmain(int argc,wchar_t **argv) { return launch(argc,argv); }
#else
int WINAPI wWinMain(HINSTANCE instance,HINSTANCE previous,PWSTR line,int show) {
    (void)instance; (void)previous; (void)line; (void)show;
    int argc=0; wchar_t **argv=CommandLineToArgvW(GetCommandLineW(),&argc);
    if(!argv) fail(L"Cannot read command line.");
    int result=launch(argc,argv); LocalFree(argv); return result;
}
#endif
