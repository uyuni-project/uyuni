#!/usr/bin/env python3
import sys
import xml.etree.ElementTree as ET

def main():
    if len(sys.argv) < 2:
        print("Usage: xmlformat.py <file>", file=sys.stderr)
        sys.exit(1)
        
    file_path = sys.argv[1]
    
    # Register namespaces to avoid ns0: prefixes
    ET.register_namespace('', 'urn:oasis:names:tc:xliff:document:1.1')
    ET.register_namespace('xsi', 'http://www.w3.org/2001/XMLSchema-instance')
    
    parser = ET.XMLParser(target=ET.TreeBuilder(insert_comments=True))
    try:
        tree = ET.parse(file_path, parser=parser)
    except Exception as e:
        print(f"Error parsing {file_path}: {e}", file=sys.stderr)
        sys.exit(1)
        
    root = tree.getroot()
    ET.indent(root, space="  ")
    
    # Write to stdout
    sys.stdout.buffer.write(ET.tostring(root, encoding='utf-8', xml_declaration=True))
    sys.stdout.buffer.write(b'\n')

if __name__ == '__main__':
    main()
