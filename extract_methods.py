import os
import re

src_dir = r"c:\Users\Anant\Downloads\TOI-space\TOI-space-TOI_Project\src\main\java\com\framework"
output_file = r"c:\Users\Anant\Downloads\TOI-space\TOI-space-TOI_Project\analysis.txt"

method_pattern = re.compile(r'^\s*(?:(?:public|private|protected|static|final|native|synchronized|abstract|transient)+\s+)+[\w\<\>\[\]]+\s+(\w+)\s*\([^\)]*\)\s*(?:throws\s+[\w\s,]+)?\s*\{', re.MULTILINE)
interface_method_pattern = re.compile(r'^\s*(?:(?:public|protected|abstract)+\s+)*[\w\<\>\[\]]+\s+(\w+)\s*\([^\)]*\)\s*(?:throws\s+[\w\s,]+)?\s*;', re.MULTILINE)

with open(output_file, "w", encoding="utf-8") as out:
    out.write("Analysis of Methods in TOI-space-TOI_Project\\src\\main\\java\\com\\framework\n")
    out.write("=========================================================================\n\n")
    for root, dirs, files in os.walk(src_dir):
        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                rel_path = os.path.relpath(file_path, src_dir)
                out.write(f"File: {rel_path}\n")
                out.write("-" * (6 + len(rel_path)) + "\n")
                try:
                    with open(file_path, "r", encoding="utf-8") as f:
                        content = f.read()
                        # simple regex might miss some or get false positives, but is a good start
                        # remove comments first
                        content = re.sub(r'//.*', '', content)
                        content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)
                        
                        methods = []
                        # basic matching for constructors and methods
                        lines = content.split('\n')
                        for line in lines:
                            line = line.strip()
                            if (line.startswith("public ") or line.startswith("private ") or line.startswith("protected ")) and "(" in line and ")" in line:
                                if "=" not in line and " class " not in line and " interface " not in line:
                                    # this is a heuristic
                                    methods.append(line)
                        if not methods:
                            out.write("  (No methods found or interface)\n")
                        else:
                            for m in methods:
                                # clean up trailing brace
                                m = m.rstrip('{').strip()
                                out.write(f"  - {m}\n")
                except Exception as e:
                    out.write(f"  Error reading file: {e}\n")
                out.write("\n")

print("Analysis complete. Saved to analysis.txt")
