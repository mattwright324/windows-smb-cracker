# windows-smb-cracker
Crack windows accounts remotely through SMB/CIFS and your provided 
credentials and password lists.

### Compatibility

The tables below show which versions of Windows can and cannot be cracked with this tool
and whether or not they have been tested.

In order to crack successfully, you should be able to successfully ping the machine.
When starting the cracker, it will also check if SMB ports are open (137,138,139,445).
A warning-overlay will show asking to continue or cancel the operation.

* **E** stands for "expected to work" when not tested.
* **N** stands for "not expected to work" when not tested.

| Windows | Tested | Works | Version | Default CIFS Enabled |
| :------ | :----: | :---: | :-----: | :------------------: |
| Server 2016 | &#10005; | E | SMB3.0 | &#10003; |
| Windows 10 | &#10003; | &#10003; | SMB3.0 | &#10003; |
| Server 2012 | &#10003; | &#10003; | SMB3.0 | &#10003; |
| Windows 8.1 | &#10005; | E | SMB3.0 | &#10003; |
| Windows 8 | &#10005; | E | SMB3.0 | &#10003; |
| Windows 7 | &#10003; | &#10003; | SMB2.1 | &#10003; |
| Server 2008 | &#10005; | E | SMB2.1 | &#10003; |
| Windows Vista |&#10003; | &#10003; | SMB2.0 | &#10003; |
| Server 2003 | &#10003; | &#10003; | SMB1.0 | &#10003; |
| Windows XP | &#10005; | ? | SMB1.0 | &#10003; |
| Windows ME | &#10005; | N | SMB1.0 | &#10005;* |
| Windows 2000 | &#10005; | N | SMB1.0 | &#10005;* |
| Server 2000 | &#10005; | N | SMB1.0 | &#10005;* |
| Windows 98 | &#10005; | N | SMB1.0 | &#10005;* |
\*CIFS Server must be manually enabled in order to connect.